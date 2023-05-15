(ns isomorphic-clojure-webapp.api.handler.users-test
  (:require [clojure.test :refer [deftest testing is are use-fixtures]]
            [ring.mock.request :as mock]
            [integrant.core :as ig]
            [integrant.repl.state :refer [system config]]
            [shrubbery.core :as shrubbery]
            [mockfn.macros :as mfn]
            [duct.database.sql]
            [isomorphic-clojure-webapp.test-helper :as helper]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [next.jdbc :as jdbc]))

#_(def ^:private alice-data {:id 1, :name "Alice", :email "alice@xample.com"})
#_(def ^:private bob-data {:id 2, :name "Bob", :email "bob@example.com"})

#_(def database-stub-normal
    (shrubbery/stub users/Users
                    {:get-users [alice-data bob-data]
                     :create-user alice-data
                     :get-user-by-id alice-data
                     :update-user (assoc alice-data :name "Alice Ackerman")
                     :delete-user alice-data}))

#_(def database-stub-incorrect-data
    (shrubbery/stub users/Users
                    {:create-user {:errors [{:code 1001
                                             :message "incorrect data"}]}
                     :get-user-by-id {:errors [{:code 1001
                                                :message "incorrect data"}]}}))

#_(def database-stub-not-found
    (shrubbery/stub users/Users
                    {:get-user-by-id {:errors [{:code 1002
                                                :message "not found"}]}
                     :update-user {:errors [{:code 1002
                                             :message "not found"}]}
                     :delete-user {:errors [{:code 1002
                                             :message "not found"}]}}))

#_(deftest users-handler-mock-test
    (testing "all "
      (let [request (mock/request :get "/api/users")
            handler (ig/init-key :isomorphic-clojure-webapp.handler.users/all
                                 {:db database-stub-normal})
            {:keys [status body]} (handler request)]
        (is (= 200 status))
        (is (= {:users [alice-data bob-data]} body))))
    (testing "create user"
      (testing "正常"
        (let [request (-> (mock/request :post "/api/users")
                          (mock/json-body (assoc alice-data :password "password")))
              handler (ig/init-key :isomorphic-clojure-webapp.handler.users/create
                                   {:db database-stub-normal})
              {:keys [status body]} (handler request)]
          (is (= 201 status))
          (is (= {:user alice-data} body))))
      (testing "データが不正"
        (let [request (-> (mock/request :post "/api/users")
                          (mock/json-body {}))
              handler (ig/init-key :isomorphic-clojure-webapp.handler.users/create
                                   {:db database-stub-incorrect-data})
              {:keys [status body]} (handler request)]
          (is (= 400 status))
          (is (= "incorrect data" (-> body :errors first :message))))))

    (testing "fetch user"
      (testing "正常"
        (let [request (mock/request :get "/api/users/1")
              handler (ig/init-key :isomorphic-clojure-webapp.handler.users/fetch
                                   {:db database-stub-normal})
              {:keys [status body]} (handler request)]
          (is (= 200 status))
          (is (= {:user alice-data} body))))

      (testing "データが見つからない"
        (let [request (mock/request :get "/api/users/10")
              handler (ig/init-key :isomorphic-clojure-webapp.handler.users/fetch
                                   {:db database-stub-not-found})
              {:keys [status body]} (handler request)]
          (is (= 404 status))
          (is (= "not found" (-> body :errors first :message))))))

    (testing "update user"
      (testing "正常"
        (let [request (-> (mock/request :update "/api/users/1")
                          (mock/json-body {:name "Alice Ackerman"}))
              handler (ig/init-key :isomorphic-clojure-webapp.handler.users/update
                                   {:db database-stub-normal})
              {:keys [status body]} (handler request)]
          (is (= 200 status))
          (is (= {:user (assoc alice-data :name "Alice Ackerman")} body))))
      (testing "データが見つからない"
        (let [request (-> (mock/request :update "/api/users/10")
                          (mock/json-body {:name "Bob"}))
              handler (ig/init-key :isomorphic-clojure-webapp.handler.users/update
                                   {:db database-stub-not-found})
              {:keys [status body]} (handler request)]
          (is (= 404 status))
          (is (= "not found" (-> body :errors first :message))))))

    (testing "delete user"
      (testing "正常"
        (let [request (mock/request :delete "/api/users/1")
              handler (ig/init-key :isomorphic-clojure-webapp.handler.users/delete {:db database-stub-normal})
              {:keys [status body]} (handler request)]
          (is (= 204 status))
          (is (= {} body))))
      (testing "データが見つからない"
        (let [request (mock/request :delete "/api/users/10")
              handler (ig/init-key :isomorphic-clojure-webapp.handler.users/delete {:db database-stub-not-found})
              {:keys [status body] :as result} (handler request)]
          (is (= 404 status))
          (is (= "not found" (-> body :errors first :message)))))))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from users"]))
    (f)))

(deftest handler-users-test
  (testing "get /users データがない場合は空のベクタを返す"
    (let [{:keys [status body]} (helper/http-get "/api/users")]
      (is (=  404 status))
      (is (=  [] (:users body)))))

  (let [{:keys [status body]} (helper/http-post "/api/users"
                                                {:name "Alice"
                                                 :email "alice@example.com"
                                                 :password "password"})
        user (:user body)
        id (:id user)]
    (testing "post /users 登録した内容を返す"
      (is (= status 201))
      (is (= "Alice" (:name user)))
      (is (= "alice@example.com" (:email user))))

    (testing "get /users/:user-id "
      (testing "取得した内容を返す"
        (let [{:keys [status body] :as all} (helper/http-get (str "/api/users/" id))]
          (is (= 200 status))
          (is (= "Alice" (-> body :user :name)))))

      (testing "対象データが無ければ not found"
        (let [{:keys [status body] :as all} (helper/http-get (str "/api/users/00000000-0000-0000-0000-000000000000"))]
          (is (= 404 status)))))

    (testing "put /users/:user-id"
      (testing "更新した内容を返す"
        (let [{:keys [status body]} (helper/http-put (str "/api/users/" id) {:name "Alice Ackerman"})]
          (is (= 200 status))
          (is (= "Alice Ackerman" (-> body :user :name)))))
      (testing "対象データが無ければ not found"
        (let [{:keys [status body]} (helper/http-put "/api/users/00000000-0000-0000-0000-000000000000" {:name "Alice Ackerman"})]
          (is (= 404 status)))))

    (testing "get /users データの配列を返す"
      (helper/http-post "/api/users"
                        {:name "Bob"
                         :email "bob@example.com"
                         :password "password"})
      (let [{:keys [status body]} (helper/http-get "/api/users")]
        (is (= 200 status))
        (is (= 2 (-> body :users count)))))

    (testing "delete"
      (testing "削除に成功したらno content"
        (let [{:keys [status body]} (helper/http-delete (str "/api/users/" id))]
          (is (= 204 status))))
      (testing "対象データが無ければ not found"
        (let [{:keys [status body]} (helper/http-delete "/api/users/00000000-0000-0000-0000-000000000000")]
          (is (= 404 status)))))))

(deftest handler-login-test
  (let [{:keys [status body]} (helper/http-post "/api/signin" {:name "alice"
                                                               :email "alice@example.com"
                                                               :password "password"})]
    (testing "正常にサインインできたら を返す"
      (is (= 201 status))))
  (testing "すでに登録済みの場合はエラーメッセージをを返す"
    #_(is (= 400)))
  (testing "内容が不正な場合はエラーメッセージをを返す"
    #_(is (= 400))))
