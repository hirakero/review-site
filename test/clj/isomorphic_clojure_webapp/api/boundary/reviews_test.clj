(ns isomorphic-clojure-webapp.api.boundary.reviews-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [isomorphic-clojure-webapp.api.boundary.reviews :as sut]
            [integrant.repl.state :refer [config system]]
            [duct.database.sql]
            [matcher-combinators.test]
            #_[matcher-combinators.clj-test :as m]
            [next.jdbc :as jdbc]))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from reviews"]))
    (f)))
;"00000000-0000-0000-0000-000000000000"
(deftest reviews-boundary-test
  (let [boundary (:duct.database.sql/hikaricp system)]
    (let [result (sut/create-review boundary {:user-id "00000000-0000-0000-0000-000000000001"
                                              :product-id "00000000-0000-0000-0000-000000000002"
                                              :title "not bad"
                                              :content "abcde"
                                              :rate 3})
          id (:id result)]
      (testing "新規作成"
        (is (= (java.util.UUID/fromString "00000000-0000-0000-0000-000000000001") (:user-id result)))
        (is (= (java.util.UUID/fromString "00000000-0000-0000-0000-000000000002") (:product-id result)))
        (is (= "not bad" (:title result)))
        (is (= "abcde" (:content result)))
        (is (= 3 (:rate result))))

      (testing "取得"
        (let [result (sut/get-review-by-id boundary id)]
          (is (= (java.util.UUID/fromString "00000000-0000-0000-0000-000000000001") (:user-id result)))
          (is (= (java.util.UUID/fromString "00000000-0000-0000-0000-000000000002") (:product-id result)))
          (is (= "not bad" (:title result)))
          (is (= "abcde" (:content result)))
          (is (= 3 (:rate result)))))

      (testing "変更"
        (let [result (sut/update-review boundary id {:content "defgh"})]
          (is (= "defgh" (:content result))))
        (let [result (sut/update-review boundary id {:title "marvelous"
                                                     :rate 5})]
          (is (= "marvelous" (:title result)))
          (is (= 5 (:rate result)))))
      (testing "全取得"
        (sut/create-review boundary {:user-id "00000000-0000-0000-0000-000000000011"
                                     :product-id "00000000-0000-0000-0000-000000000012"
                                     :title "too much pain"
                                     :content "b"
                                     :rate 1})
        (sut/create-review boundary {:user-id "00000000-0000-0000-0000-000000000021"
                                     :product-id "00000000-0000-0000-0000-000000000022"
                                     :title "pretty cute"
                                     :content "c"
                                     :rate 2})
        (sut/create-review boundary {:user-id "00000000-0000-0000-0000-000000000021"
                                     :product-id "00000000-0000-0000-0000-000000000022"
                                     :title "nobody knows"
                                     :content "e"
                                     :rate 3})
        (let [result (sut/get-reviews boundary {})]
          (is (= 4 (count result))))

        (testing "削除"
          (let [result (sut/delete-review boundary "00000000-0000-0000-0000-000000000111")]
            (is (nil? result)))
          (let [result (sut/delete-review boundary id)]
            (is (= id (:id result))))
          (let [result (sut/get-reviews boundary {})]
            (is (= 3 (count result)))))))

    (testing "絞り込み")))