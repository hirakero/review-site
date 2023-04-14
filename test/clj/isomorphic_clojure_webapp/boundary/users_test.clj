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
      #_(jdbc/execute! ds ["delete from users"])
      (let [result (jdbc/execute! ds ["select * from users"])]
        result)
      
      #_boundary
      #_ds
      )
    (f)
    ))
;[#:next.jdbc{:update-count 2}]
(deftest users-boundary-test 
  (let [boundary (:duct.database.sql/hikaricp system)] 
    (testing "create 正常"
      (let [result (users/create-user boundary {:name "Alice"})] 
        (is (pos-int? (-> result first :id)))
        (is (= "Alice" (-> result first :name)))
        (is (nil? (:errors result)))
        result)) 
    (testing "create 異常"
      (let [result (users/create-user boundary {:namae "Aida"})]
        (is (pos-int? (-> result :errors first :code)))
        #_(is (= "Alice" (-> result first :name)))
        result)
      )))