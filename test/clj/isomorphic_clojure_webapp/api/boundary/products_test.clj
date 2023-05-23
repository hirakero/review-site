(ns isomorphic-clojure-webapp.api.boundary.products-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [isomorphic-clojure-webapp.api.boundary.products :as sut]
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

(comment
  (let [boundary (:duct.database.sql/hikaricp system)
        ds (-> boundary :spec :datasource)]
    (jdbc/execute! ds #_[" SELECT * FROM products LIMIT ?" 4]
                   #_[" SELECT * FROM products OFFSET ?" 2]
                   [" SELECT * FROM products LIMIT ? OFFSET ?" 2 2])))

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
            (let [result (sut/update-product boundary id {:name "Torque X"})]
              (is (= "Torque X"  (:name result)))
              result))
          (testing "対象データが無いときはnil"
            (let [result (sut/update-product boundary "00000000-0000-0000-0000-000000000000" {:name "Sparrow"})]
              (is (= nil result))
              result)))

        (testing "all"
          (sut/create-product boundary {:name "Slant Roller"
                                        :description "small"})
          (sut/create-product boundary {:name "Flippin' Pickle"
                                        :description "pfs"})
          (sut/create-product boundary {:name "Stylus Revolve"
                                        :description "thin profile"})
          (sut/create-product boundary {:name "Beanflip Ocularis"
                                        :description "thin Asymmetrical"})
          (let [result (sut/get-products boundary {})]
            (is (= 5 (count result)))))

        (testing "filter"
          (let [result (sut/get-products boundary {:name "R"})]
            (is (= ["Slant Roller" "Stylus Revolve"] (map #(:name %) result))))
          (let [result (sut/get-products boundary {:description "Asymmetric"})]
            (is (= ["Torque X" "Beanflip Ocularis"] (map #(:name %) result)))))

        (testing "limit offset"
          (let [result (sut/get-products boundary {:limit 4})]
            (is (= 4 (count result))))
          (let [result (sut/get-products boundary {:offset 2})]
            (is (= 3 (count result))))
          (let [result (sut/get-products boundary {:offset 2 :limit 2})]
            (is (= 2 (count result))))

          (testing "delete"
            (testing "正常"
              (let [result (sut/delete-product boundary id)]
                (is (= id (:id result)))))
            (testing "対象データが無いときはnil"
              (let [result (sut/delete-product boundary "00000000-0000-0000-0000-000000000000")]
                (is (= nil result))))))))))
