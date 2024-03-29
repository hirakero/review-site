(ns isomorphic-clojure-webapp.api.handler.app-test
  (:require
   [clojure.test :refer [deftest testing is are use-fixtures]]
   [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]))

(deftest handler-app-test
  (testing "tokenのテスト"
    (let [base-url "http://localhost:3000"
          _ (helper/http-post (str base-url "/api/users") {:name "Bob"
                                                           :email "bob@example.com"
                                                           :password "password"})
          {:keys [status body]} (helper/http-post (str base-url "/api/signin") {:name "Bob"
                                                                                :email "bob@example.com"
                                                                                :password "password"})
          token (:token body)]

      (testing "get /api/health はtokenありでも成功"
        (let [{:keys [status body]} (helper/http-get (str base-url "/api/health")
                                                     {"authorization" (str "Token " token)})]
          (is (= 200 status))
          (is (= "running!" (:message body)))
          (is ((complement nil?) (:token body)))))

      (testing "get /api/health はtokenなしでも成功"
        (let [{:keys [status body]} (helper/http-get (str base-url "/api/health"))]
          (is (= 200 status))
          (is (= "running!" (:message body)))
          (is (nil? (:token body)))))

      (testing "get /api/health-auth はtokenなしは401(認証自体の失敗)"
        (let [{:keys [status body]} (helper/http-get (str base-url "/api/health-auth"))]

          (is (= 401 status))))

      (testing "getは /api/health-auth tokenありは200 "
        (let [{:keys [status body]} (helper/http-get (str base-url "/api/health-auth")
                                                     {"authorization" (str "Token " token)})]
          (is (= 200 status))
          (is (= "running!" (:message body))))))))

