(ns isomorphic-clojure-webapp.boundary.users-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [isomorphic-clojure-webapp.boundary.users :as users]
            [integrant.repl.state :refer [config system]]
            [duct.database.sql]
            [next.jdbc :as jdbc]))


(use-fixtures :each
  (fn [f] 
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from users"])
      #_(let [result (jdbc/execute! ds ["select * from users"])]
        result) 
      )
    (f)
    ))
(deftest users-boundary-test 
  (let [boundary (:duct.database.sql/hikaricp system)] 
    (testing "create"
      (let [result (-> (users/create-user boundary {:name "Alice"}) first)
            id (:id result)]
        (is (pos-int? id))
        (is (= "Alice" (:name result)))
        #_(is (nil? (:errors result)))

        (testing "fetch"
          (let [result (-> (users/get-user-by-id boundary id) first)]
            (is (= id (:id result)))
            (is (= "Alice" (:name result)))))
        (testing "update"
          (let [result (-> (users/update-user boundary id {:name "Alice Abbot"}) first)]
            (is (= "Alice Abbot" (:name result)))
            result))
        (testing "delete"
          (let [result (-> (users/delete-user boundary id) first)]
            (is (= id (:id result)))
            result))))
    (testing "create 異常"
      (let [result (users/create-user boundary {:namae "Aida"})]
        #_(is ( (-> result)))
        #_(is (= "Alice" (-> result first :name)))
        )
      )))