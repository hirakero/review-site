(ns isomorphic-clojure-webapp.handler.users-test
  (:require [clojure.test :refer [deftest testing is]] 
            [ring.mock.request :as mock]
            [integrant.core :as ig]))

(deftest users-handler-test
  (testing "create user" 
    (let [request (-> (mock/request :post "/users")
                      (mock/json-body {:name "Alice"}))
          handler (ig/init-key :isomorphic-clojure-webapp.handler.users/create {})
          {:keys [status body]} (handler request)]
      (is (= 201 status))
      (is (= 1 (:user-id body))))) 
  (testing "fetch user"
    (let [request (mock/request :get "/users/1")
          handler (ig/init-key :isomorphic-clojure-webapp.handler.users/fetch {})
          {:keys [status body]} (handler request)]
      (is (= 200 status))
      (is (= {:id 1 :name "Alice"} body)))))
