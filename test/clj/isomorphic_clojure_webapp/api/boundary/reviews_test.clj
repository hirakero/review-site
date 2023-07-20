(ns isomorphic-clojure-webapp.api.boundary.reviews-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [isomorphic-clojure-webapp.api.boundary.reviews :as sut]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [isomorphic-clojure-webapp.api.boundary.products :as products]
            [integrant.repl.state :refer [config system]]
            [duct.database.sql]
            [matcher-combinators.test]
            [next.jdbc :as jdbc]))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from users"])
      (jdbc/execute! ds ["delete from products"])
      (jdbc/execute! ds ["delete from reviews"]))
    (f)))

;"00000000-0000-0000-0000-000000000000"
(comment
  (users/create-user (:duct.database.sql/hikaricp system) {:name "Alice"
                                                           :email "alice@email.com"
                                                           :password "password"})

  (sut/get-reviews-by-product (:duct.database.sql/hikaricp system) "00000000-0000-0000-0000-000000000000")
  (sut/get-review-by-id (:duct.database.sql/hikaricp system) "00000000-0000-0000-0000-000000000000")
  (sut/delete-review (:duct.database.sql/hikaricp system) "00000000-0000-0000-0000-000000000000"))

(deftest reviews-boundary-test
  (let [boundary (:duct.database.sql/hikaricp system)]
    (let [{user1-id :id} (users/create-user boundary {:name "Alice"
                                                      :email "alice@email.com"
                                                      :password "password"})
          {user2-id :id} (users/create-user boundary {:name "Bob"
                                                      :email "bob@email.com"
                                                      :password "password"})
          {user3-id :id} (users/create-user boundary {:name "CHris"
                                                      :email "chris@email.com"
                                                      :password "password"})

          {product1-id :id} (products/create-product boundary {:name "thinkpad"
                                                               :description "black"})

          {product2-id :id}  (products/create-product boundary {:name "macbook"
                                                                :description "silver"})

          {product3-id :id}  (products/create-product boundary {:name "surface"
                                                                :description "tablet"})

          result (sut/create-review boundary {:user-id user1-id
                                              :product-id product1-id
                                              :title "not bad"
                                              :content "abcde"
                                              :rate 3})
          id (:id result)]
      (testing "新規作成"
        (is (=  user1-id (:user-id result)))
        (is (=  product1-id (:product-id result)))
        (is (= "not bad" (:title result)))
        (is (= "abcde" (:content result)))
        (is (= 3 (:rate result))))

      (testing "取得"
        (let [result (sut/get-review-by-id boundary id)]
          (is (=  user1-id (:user-id result)))
          (is (=  product1-id (:product-id result)))
          (is (= "not bad" (:title result)))
          (is (= "abcde" (:content result)))
          (is (= 3 (:rate result)))
          ;TODO product-name
          ;TODO user-name
          ))

      (testing "変更"
        (let [result (sut/update-review boundary id {:content "defgh"})]
          (is (= "defgh" (:content result))))
        (let [result (sut/update-review boundary id {:title "marvelous"
                                                     :rate 5})]
          (is (= "marvelous" (:title result)))
          (is (= 5 (:rate result)))))

      (testing "全取得"
        (let [review2-id (sut/create-review boundary {:user-id user1-id
                                                      :product-id product2-id
                                                      :title "too much pain"
                                                      :content "b"
                                                      :rate 1})
              _ (sut/create-review boundary {:user-id user2-id
                                             :product-id product1-id
                                             :title "pretty cute"
                                             :content "c"
                                             :rate 2})
              #_#__ (sut/create-review boundary {:user-id user2-id
                                                 :product-id product2-id
                                                 :title "nobody knows"
                                                 :content "e"
                                                 :rate 3})]

          (let [result (sut/get-reviews boundary {})]
            (is (= 3 (count result))))

          (testing "product 指定の取得"
            (testing ""
              (let [results (sut/get-reviews-by-product boundary product1-id)]
                (is (vector? results))
                (is (= 2 (count results)))

                (let [review (-> (filter #(= (:id %) id) results)
                                 first)]
                  (is (= "Alice" (:user-name review)))
                  (is (= "marvelous" (:title review))))))
            (testing ""
              (let [results (sut/get-reviews-by-product boundary product3-id)]
                (is (empty? results)))))

          (testing "user 指定の取得"
            (testing ""
              (let [results (sut/get-reviews-by-user boundary user2-id)]
                (is (vector? results))
                (is (= 1 (count results)))
                (let [review (first results)]
                  (is (= "thinkpad" (:product-name review)))
                  (is (= "pretty cute" (:title review))))))
            (testing ""
              (let [results (sut/get-reviews-by-user boundary user3-id)]
                (is (empty? results)))))

          (testing "削除"
            (testing "存在しないデータの削除はnil"
              (let [result (sut/delete-review boundary "00000000-0000-0000-0000-000000000111")]
                (is (nil? result))))
            (let [result (sut/delete-review boundary id)]
              (is (= id (:id result))))
            (let [result (sut/get-reviews boundary {})]
              (is (= 2 (count result)))))))

      (testing "絞り込み"))))