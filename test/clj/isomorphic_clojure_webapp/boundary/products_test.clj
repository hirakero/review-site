(ns isomorphic-clojure-webapp.boundary.products-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [isomorphic-clojure-webapp.boundary.products :as sut]
            [integrant.repl.state :refer [config system]]
            [duct.database.sql]
            [matcher-combinators.test]
            [matcher-combinators.clj-test :as m]
            [next.jdbc :as jdbc]))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from products"]))
    (f)))

(deftest products-boundary-test
  (let [boundary (:duct.database.sql/hikaricp system)]
    (testing "create"
      (let [result (sut/create-product boundary {:name "Torque"
                                                 :description "Asymmetric design"})
            id (:id result)]
        (is (= java.util.UUID (type id)))
        (is (= "Torque" (:name result)))
        (is (= "Asymmetric design" (:description result)))

        (testing "fetch"
          (testing "正常"
            (let [result  (sut/get-product-by boundary :id id)]
              (is (= "Torque" (:name result)))))
          (testing "対象データが無いときはnil"
            (let [result  (sut/get-product-by boundary :name "abc")]
              (is (= nil  result))))
          (testing "例外が発生したらExceptionInfoで返す"
            (is (thrown-with-msg? clojure.lang.ExceptionInfo #"err"
                                  (sut/get-product-by boundary :id "abc")))
            (let [e (try (sut/get-product-by boundary :id "abc")
                         (catch clojure.lang.ExceptionInfo e
                           e))]
              (is (not (empty?  (ex-data e)))))))

        (testing "update"
          (testing "正常"
            (let [result (sut/update-product boundary id {:name "Sparrow"})]
              (is (= "Sparrow"  (:name result)))
              result))
          (testing "対象データが無いときはnil"
            (let [result (sut/update-product boundary "00000000-0000-0000-0000-000000000000" {:name "Sparrow"})]
              (is (= nil result))
              result)))

        (testing "all"
          (sut/create-product boundary {:name "Slant Roller"
                                        :description "small"})
          (let [result (sut/get-products boundary {})]
            (is (= 2 (count result)))))

        (testing "delete"
          (testing "正常"
            (let [result (sut/delete-product boundary id)]
              (is (= id (:id result)))))
          (testing "対象データが無いときはnil"
            (let [result (sut/delete-product boundary "00000000-0000-0000-0000-000000000000")]
              (is (= nil result)))))))))
