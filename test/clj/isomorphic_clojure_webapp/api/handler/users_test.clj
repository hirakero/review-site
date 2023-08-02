(ns isomorphic-clojure-webapp.api.handler.users-test
  (:require [clojure.test :refer [deftest testing is are use-fixtures]]
            [ring.mock.request :as mock]
            [integrant.core :as ig]
            [integrant.repl.state :refer [system config]]
            [shrubbery.core :as shrubbery]
            [mockfn.macros :as mfn]
            [duct.database.sql]
            [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [next.jdbc :as jdbc]
            [matcher-combinators.clj-test]
            [isomorphic-clojure-webapp.api.auth :as auth]))

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
    (testing "list "
      (let [request (mock/request :get "/api/users")
            handler (ig/init-key :isomorphic-clojure-webapp.handler.users/list
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
  (let [base-url "http://localhost:3000"]
    (testing "get /users データがない場合は何も返さない"
      (let [{:keys [status body]} (helper/http-get (str base-url "/api/users"))]
        (is (=  404 status))
        (is (nil? (:users body)))
        (is (= "users not found" (:error body)))))

    (let [{:keys [status headers body]} (helper/http-post (str base-url "/api/users")
                                                          {:name "Alice"
                                                           :email "alice@example.com"
                                                           :password "password"})
          user1-id (:id body)
          user1-token-header {"authorization" (str "Token " (:token body))}
          {user2-body :body} (helper/http-post (str base-url "/api/users")
                                               {:name "Bob"
                                                :email "bob@example.com"
                                                :password "password"})
          user2-token-header {"authorization" (str "Token " (:token user2-body))}
          user0-token-header {"authorization" (str "Token " (auth/create-token {:id "00000000-0000-0000-0000-000000000000" :name "name"}))}]
      (testing "post /users 登録した内容を直接返す"
        (is (= status 201))
        (is (get headers "location"))
        (is (= "Alice" (:name body)))
        (is (= "alice@example.com" (:email body))))

      (testing "get /users/:user-id "
        (testing "取得した内容を返す"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/users/" user1-id))]
            (is (= 200 status))
            (is (= "Alice" (-> body :user :name)))))

        (testing "対象データが無ければ not foundで何も返さない"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/users/00000000-0000-0000-0000-000000000000"))]
            (is (= 404 status))
            (is (nil? (:user body)))
            (is (= "user not found" (:error body))))))

      (testing "put /users/:user-id"
        (testing "authorizatin haderがなければ401"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/users/" user1-id)
                                                       {:name "Alice Ackerman"})]
            (is (= 401 status))
            (is (= "login user only" (:error body)))))

        (testing "他のユーザーは変更できない403"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/users/" user1-id)
                                                       user2-token-header
                                                       {:name "Alice Ackerman"})]
            (is (= 403 status))
            (is (= "owner only" (:error body)))))

        (testing "更新した内容を直接返す"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/users/" user1-id)
                                                       user1-token-header
                                                       {:name "Alice Ackerman"})]
            (is (= 200 status))
            (is (= "Alice Ackerman" (-> body :name)))))
        (testing "対象データが無ければ not found"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/users/00000000-0000-0000-0000-000000000000")
                                                       user0-token-header
                                                       {:name "Alice Ackerman"})]
            (is (= 404 status))
            (is (= "user not found" (:error body))))))

      (testing "get /users データの配列を返す"
        (helper/http-post (str base-url "/api/users")
                          {:name "Bob"
                           :email "bob@example.com"
                           :password "password"})
        (let [{:keys [status body]} (helper/http-get (str base-url "/api/users"))]
          (is (= 200 status))
          (is (vector? (-> body :users)))
          (is (= 2 (-> body :users count)))))

      (testing "delete"
        (testing "authorizatin haderがなければ401"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/users/" user1-id)
                                                          {:name "Alice Ackerman"})]
            (is (= 401 status))
            (is (= "login user only" (:error body)))))

        (testing "他のユーザーは変更できない403"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/users/" user1-id)
                                                          user2-token-header)]
            (is (= 403 status))
            (is (= "owner only" (:error body)))))
        (testing "削除に成功したらno content で何も返さない"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/users/" user1-id)
                                                          user1-token-header)]
            (is (= 204 status))
            (is (nil? body))))
        (testing "対象データが無ければ not found"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/users/00000000-0000-0000-0000-000000000000")
                                                          user0-token-header)]
            (is (= 404 status))
            (is (= "user not found" (:error body)))))))))


(comment
  (auth/create-token {:name "alice"
                      :email "alice@example.com"
                      :password "password"})
  (def base-url "http://localhost:3000"))
(def jwt-regex #"^[A-Za-z0-9-_]+?.[A-Za-z0-9-_]+?.[A-Za-z0-9-_]+$")
(deftest handler-auth-test
  (let [base-url "http://localhost:3000"]
    (testing "サインアップ"
      (testing "正常、ユーザー情報とトークンを返す。パスワードは返さない"
        (let [{:keys [status body]} (helper/http-post (str base-url "/api/users")
                                                      {:name "alice"
                                                       :email "alice@example.com"
                                                       :password "password"})]
          (is (= 201 status))
          (is (match? {:name "alice"
                       :email "alice@example.com"}
                      body))
          (is (not (contains?  body :password)))
          (is (boolean (re-find jwt-regex (:token body ""))))))
      (testing "内容が不正なら400"
        (let [{:keys [status body]} (helper/http-post (str base-url "/api/users")
                                                      {:namae "alice"
                                                       :e-mail "alice@example.com"
                                                       :password 5})]
          (is (= 400 status))
          (is (contains? body :spec))))
      (testing "既に登録されていたら409?"
        (let [{:keys [status body]} (helper/http-post (str base-url "/api/users")
                                                      {:name "alice"
                                                       :email "alice@example.com"
                                                       :password "password"})]
          (is (= 409 status))
          (is (= "user already exists" (:error body))))))

    (testing "サインイン"
      (testing "正常、ユーザー情報とトークンを返す。パスワードは返さない"
        (let [{:keys [status body]} (helper/http-post (str base-url "/api/signin")
                                                      {:name "alice"
                                                       :email "alice@example.com"
                                                       :password "password"})
              user (:user body)]

          (is (= 200 status))
          (is (match? {:name "alice"
                       :email "alice@example.com"}
                      user))
          (is (not (contains? user :password)))
          (let [claim  (auth/parse-token (:token body))]
            (is (= (:id user) (:sub claim)))
            (is (= (:name user) (:name claim)))))

        (let [{:keys [status body]} (helper/http-post (str base-url "/api/signin")
                                                      {:name "alice"
                                                       :password "password"})]

          (is (= 200 status)))
        (let [{:keys [status body]} (helper/http-post (str base-url "/api/signin")
                                                      {:email "alice@example.com"
                                                       :password "password"})]

          (is (= 200 status))))
      (testing "内容が不正なら400"
        (let [{:keys [status body]} (helper/http-post (str base-url "/api/signin")
                                                      {:me-ru "alice@example.com"
                                                       :pw "password"})]

          (is (= 400 status)))))
    (testing "未登録のユーザーはエラーメッセージを返す"
      (let [{:keys [status body]} (helper/http-post (str base-url "/api/signin")
                                                    {:name "dave"
                                                     :password "password"})]

        (is (= 401 status))))))
 
