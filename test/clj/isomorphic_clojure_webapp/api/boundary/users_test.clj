(ns isomorphic-clojure-webapp.api.boundary.users-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [integrant.repl.state :refer [config system]]
            [duct.database.sql]
            [matcher-combinators.test]
            [matcher-combinators.clj-test :as m]
            [next.jdbc :as jdbc]))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from users"]))
    (f)))

(comment
  (#(let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]

      (let [result (jdbc/execute! ds [%])]
        result))
   #_"select * from users"
   #_"select * from products"
   #_"select uuid('00000000-0000-0000-0000-000000000000')"
   "select * from products where id = '04875e11-0591-4a1b-ab4f-287c3367cf29'"
   #_"insert into users (name) values ('Bob')"
   #_"delete from users"))

(deftest users-boundary-test
  (let [boundary (:duct.database.sql/hikaricp system)]
    (testing "all"
      (let [result (users/get-users boundary)]
        (is (= [] result))))
    (testing "create"
      (let [result (users/create-user boundary {:name "Alice"
                                                :email "alice@example.com"
                                                :password "password"})
            id (:id result)]

        (is (= java.util.UUID (type id)))
        (is (= "Alice" (:name result)))
        (is (= "alice@example.com" (:email result)))
        (is (nil? (:password result)))
        (is (not (nil? (:created result))))
        (is (not (nil? (:updated result))))

        (testing "fetch"
          (let [result  (users/get-user-by boundary :id id)]
            (is (= id (:id result)))
            (is (= "Alice" (:name result)))
            (is (= "alice@example.com" (:email result)))
            (is (nil? (:password result)))
            (is (not (nil? (:created result))))
            (is (not (nil? (:updated result))))))

        (testing "update"
          (let [result (users/update-user boundary id {:name "Alice Abbot"})]
            (is (= "Alice Abbot"  (:name result)))
            result)
          (let [result (users/update-user boundary id {:email "a.abbot@example.com"})]
            (is (= "a.abbot@example.com"  (:email result)))
            result)
          (let [result (users/update-user boundary id {:name "Alice"
                                                       :email "alice@example.com"})]
            (is (match? {:name "Alice"
                         :email "alice@example.com"} result))))
        (testing "all"
          (users/create-user boundary {:name "Bob"
                                       :email "bob@examile.com"
                                       :password "password"})
          (let [result (users/get-users boundary)]
            (is (= 2 (count result)))
            (is (empty? (remove #(nil? (:password %)) result)))))
        (testing "delete"
          (let [result (users/delete-user boundary id)]
            (is (= id (:id result)))
            (is (nil? (:password result)))))))
    #_(testing "create 異常"
        (let [result (users/create-user boundary {:namae "Aida"})]
          #_(is ((-> result)))
          #_(is (= "Alice" (-> result first :name)))))))