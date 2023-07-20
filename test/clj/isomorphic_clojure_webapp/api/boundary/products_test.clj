(ns isomorphic-clojure-webapp.api.boundary.products-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [isomorphic-clojure-webapp.api.boundary.products :as sut]
            [integrant.repl.state :refer [config system]]
            [duct.database.sql]
            [matcher-combinators.test]
            [next.jdbc :as jdbc]))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from products"]))
    (f)))

(comment
  (let [boundary (:duct.database.sql/hikaricp system)
        ds (-> boundary :spec :datasource)
        results (jdbc/execute! ds
                               #_[" SELECT * FROM products LIMIT ? OFFSET ?" 2 2]
                               {:return-keys true
                                :builder-fn next.jdbc.result-set/as-unqualified-maps})
        names (map #(:name %) results)]
    names))

(deftest products-boundary-test
  (let [boundary (:duct.database.sql/hikaricp system)]
    (let [result (sut/create-product boundary {:name "Torque"
                                               :description "Asymmetric design"})
          id (:id result)]
      (testing "create"
        (is (= java.util.UUID (type id)))
        (is (= "Torque" (:name result)))
        (is (= "Asymmetric design" (:description result))))

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

      (testing "list"
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

      (testing "order"
        (let [names ["Beanflip Ocularis" "Flippin' Pickle" "Slant Roller" "Stylus Revolve" "Torque X"]
              key-asc-result (sut/get-products boundary {:sort :name})

              key-desc-result (sut/get-products boundary {:sort :name :order :desc})]
          (is (= names (map #(:name %) key-asc-result)))
          (is (= (reverse names) (map #(:name %) key-desc-result)))))

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
          (is (= 2 (count result)))))
      #_(doseq [i (range 10)]
          (sut/create-product boundary {:name (str i)
                                        :description ""}))
      #_(testing "has-prev,has-next"
          (let [{:keys [has-next has-prev]} (sut/get-products boundary {:limit 5})]
            (is (false? has-prev))
            (is (true? has-next))))

      (doseq [i (range 10 200)]
        (sut/create-product boundary {:name (str i)
                                      :description ""}))
      (testing "limitの上限あり"
        (let [result (sut/get-products boundary {:limit 200})]
          (is (= 100 (count result)))))

      (testing "delete"
        (testing "正常"
          (let [result (sut/delete-product boundary id)]
            (is (= id (:id result)))))
        (testing "対象データが無いときはnil"
          (let [result (sut/delete-product boundary "00000000-0000-0000-0000-000000000000")]
            (is (= nil result))))))))
