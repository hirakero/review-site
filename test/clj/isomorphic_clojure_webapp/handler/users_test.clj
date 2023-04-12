(ns isomorphic-clojure-webapp.handler.users-test
  (:require [clojure.test :refer [deftest testing is]]
            [ring.mock.request :as mock]
            [integrant.core :as ig]
            [integrant.repl.state :refer [system config]]
            [shrubbery.core :as shrubbery]
            [mockfn.macros :as mfn]
            [duct.database.sql]
            [isomorphic-clojure-webapp.boundary.users :as users]))

(def database-stub-normal
  (shrubbery/stub users/Users
                  {:create-user {:id 1}
                   :get-user-by-id {:id 1 :name "Alice"}
                   :update-user {:id 1}
                   :delete-user {}}))

(def database-stub-incorrect-data
  (shrubbery/stub users/Users
                  {:create-user {:errors [{:code 1001
                                           :message "incorrect data"}]}
                   :get-user-by-id {:errors [{:code 1001
                                              :message "incorrect data"}]}}))

(def database-stub-not-found
  (shrubbery/stub users/Users
                  {:get-user-by-id {:errors [{:code 1002
                                              :message "not found"}]}
                   :update-user {:errors [{:code 1002
                                           :message "not found"}]}
                   :delete-user {:errors [{:code 1002
                                           :message "not found"}]}}))

(defn get-key [config {:keys [request-method uri]}]
  (-> config
      :duct.router/reitit
      :routes
      ((fn [v]  (filter #(= (first %) uri) v)))
      (first)
      (second)
      request-method
      :key))

(deftest users-handler-test
  (testing "create user"
    (testing "正常"
      (let [request (-> (mock/request :post "/users")
                        (mock/json-body {:name "Alice"}))
            key (get-key config request)
            handler (ig/init-key key {:db database-stub-normal})
            {:keys [status body]} (handler request)]
        (is (= 201 status))
        (is (= 1 (:id body)))))
    (testing "データが不正"
      (let [request (-> (mock/request :post "/users")
                        (mock/json-body {}))
            key (get-key config request)
            handler (ig/init-key key {:db database-stub-incorrect-data})
            {:keys [status body]} (handler request)]
        (is (= 400 status))
        (is (= "incorrect data" (-> body :errors first :message))))))

  (testing "fetch user"
    (testing "正常"
      (let [request (mock/request :get "/users/1")
            handler (ig/init-key :isomorphic-clojure-webapp.handler.users/fetch 
                                 {:db database-stub-normal})
            {:keys [status body]} (handler request)]
        (is (= 200 status))
        (is (= {:id 1 :name "Alice"} body))))
    
    (testing "データが見つからない"
      (let [request (mock/request :get "/users/10")
            handler (ig/init-key :isomorphic-clojure-webapp.handler.users/fetch 
                                 {:db database-stub-not-found})
            {:keys [status body]} (handler request)]
        (is (= 404 status))
        (is (= "not found" (-> body :errors first :message))))))
  
  (testing "update user"
    (testing "正常"
      (let [request (-> (mock/request :update "/users/1")
                        (mock/json-body {:name "Bob"}))
            handler (ig/init-key :isomorphic-clojure-webapp.handler.users/update
                                 {:db database-stub-normal})
            {:keys [status body]} (handler request)]
        (is (= 200 status))
        (is (= 1 (:id body)))))
    (testing "データが見つからない"
      (let [request (-> (mock/request :update "/users/10")
                        (mock/json-body {:name "Bob"}))
            handler (ig/init-key :isomorphic-clojure-webapp.handler.users/update
                                 {:db database-stub-not-found})
            {:keys [status body]} (handler request)]
        (is (= 404 status))
        (is (= "not found" (-> body :errors first :message))))))

  (testing "delete user"
    (testing "正常"
      (let [request (mock/request :delete "/users/1") 
            handler (ig/init-key :isomorphic-clojure-webapp.handler.users/delete {:db database-stub-normal})
            {:keys [status body]} (handler request)]
        (is (= 204 status))
        (is (= {} body))))
    (testing "データが見つからない"
      (let [request (mock/request :delete "/users/10")
            handler (ig/init-key :isomorphic-clojure-webapp.handler.users/delete {:db database-stub-not-found})
            {:keys [status body] :as result} (handler request)]
        (is (= 404 status))
        (is (= "not found" (-> body :errors first :message)))))
    ))