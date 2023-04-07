(ns isomorphic-clojure-webapp.handler.users-test
  (:require [clojure.test :refer [deftest testing is]]
            [ring.mock.request :as mock]
            [integrant.core :as ig]
            [integrant.repl.state :refer [system config]]
            [shrubbery.core :as shrubbery]
            [isomorphic-clojure-webapp.boudary.users :as users]))

(def database-stub
  (shrubbery/stub users/Users
                  {:get-user-by-id {:id 1 :name "Alice"}
                   :create-user {:user-id 1}}))

(defn get-key [config {:keys [request-method uri]}] 
  (-> config
      :duct.router/reitit
      :routes 
      ((fn [v]  (filter #(= (first %) uri) v)))
      (first)
      (second)
      request-method
      :key))
(get-key config {:request-method :get :uri "/users/1"} )

(deftest users-handler-test
  (testing "create user" 
    (let [request (-> (mock/request :post "/users")
                      (mock/json-body {:name "Alice"}))
          key (get-key config request)
          handler (ig/init-key key {:db database-stub})
          {:keys [status body]} (handler request)]
      (is (= 201 status))
      (is (= 1 (:user-id body))))) 
  (testing "fetch user"
    (let [request (mock/request :get "/users/1")
          handler (ig/init-key :isomorphic-clojure-webapp.handler.users/fetch {:db database-stub})
          {:keys [status body]} (handler request)]
      (is (= 200 status))
      (is (= {:id 1 :name "Alice"} body)))))
