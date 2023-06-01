(ns isomorphic-clojure-webapp.ui.boundary.products-test
  (:require [clojure.test :refer [deftest testing is are use-fixtures]]
            [isomorphic-clojure-webapp.ui.boundary.products :as sut]
            [matcher-combinators.test]
            [integrant.repl.state :refer [config system]]
            [matcher-combinators.clj-test :as m]
            [next.jdbc :as jdbc]))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from products"]))
    (f)))

(deftest ui-boundary-product-test
  (let [{:keys [status body]} (sut/create-product {:name "panda" :description "white and black"})
        id (:id body)]
    (testing "create"
      (is (= "panda" (:name body)))
      (is (= "white and black" (:description body))))

    (testing "fetch"
      (let [{:keys [status body]} (sut/get-product-by-id id)
            product (:product body)]
        (is (= "panda" (:name product)))
        (is (= "white and black" (:description product)))))

    (testing "update"
      (let [{:keys [status body]} (sut/update-product id {:description "eat bamboo"})]
        (is (= "panda" (:name body)))
        (is (= "eat bamboo" (:description body)))))

    (sut/create-product {:name "lesser panda" :description "red"})
    (sut/create-product {:name "fox" :description "yellow"})

    (testing "get-all"
      (let [{:keys [status body]} (sut/get-products {})
            products (:products body)]
        (is (= 3 (count products))))
      (let [{:keys [status body]} (sut/get-products {:name "panda"})
            names (map #(:name %) (:products body))]
        (is (= ["panda" "lesser panda"] names))))
    (testing "delete"
      (let [{:keys [status body]} (sut/delete-product id)]
        (is (= 204 status))
        (let [{:keys [status body]} (sut/get-products {})
              products (:products body)]
          (is (= 2 (count products))))))))
