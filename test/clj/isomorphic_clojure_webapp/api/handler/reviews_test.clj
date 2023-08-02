(ns isomorphic-clojure-webapp.api.handler.reviews-test
  (:require [clojure.test :refer [deftest testing is are use-fixtures]]
            [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]
            [integrant.core :as ig]
            [integrant.repl.state :refer [system config]]
            [next.jdbc :as jdbc]
            [matcher-combinators.clj-test]
            [isomorphic-clojure-webapp.api.auth :as auth]
            [ring.mock.request :as mock]))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from reviews"])
      (jdbc/execute! ds ["delete from users"])
      (jdbc/execute! ds ["delete from products"]))
    (f)))

(comment
  (def base-url "http://localhost:3000"))
(deftest handler-reviews-test
  (let [base-url "http://localhost:3000"]

    (let [;正規ユーザー追加
          {{user1-id :id}  :body} (helper/http-post (str base-url "/api/users")
                                                    {:name "Chris"
                                                     :email "chris@email.com"
                                                     :password "password"})

        ;サインイン、トークン保持
          {{user1-token :token} :body} (helper/http-post (str base-url "/api/signin")
                                                         {:name "Chris"
                                                          :email "chris@email.com"
                                                          :password "password"})
          user1-token-header {"authorization" (str "Token " user1-token)}

        ;他のユーザー追加
          _  (helper/http-post (str base-url "/api/users")
                               {:name "Dave"
                                :email "dave@email.com"
                                :password "password"})

          {{user2-token :token} :body} (helper/http-post (str base-url "/api/signin")
                                                         {:name "Dave"
                                                          :email "dave@email.com"
                                                          :password "password"})

          user2-token-header {"authorization" (str "Token " user2-token)}

          resultu3 (helper/http-post (str base-url "/api/users")
                                     {:name "Eric"
                                      :email "eric@email.com"
                                      :password "password"})
          {{user3-id :id}  :body} resultu3
        ;商品追加
          {{product1-id :id} :body} (helper/http-post (str base-url "/api/products")
                                                      {:name "sr400"
                                                       :description "single 400cc"})

          {{product2-id :id} :body} (helper/http-post (str base-url "/api/products")
                                                      {:name "monkey"
                                                       :description "single 50cc"})

          {{product3-id :id} :body} (helper/http-post (str base-url "/api/products")
                                                      {:name "dax 125"
                                                       :description "single 125cc"})
        ;レビュー追加
          result (helper/http-post (str base-url "/api/products/" product1-id "/reviews")
                                   user1-token-header
                                   {:title "not bad"
                                    :content "Today, I wanna show you ...."
                                    :rate 4})
          review-p1u1-id (-> result :body :id)]

      (testing "post /products/:product-id/reviews"
        (testing "正常。201 とlocationと、登録した内容を直接返す"
          (let [{:keys [status body headers]} result]
            (is (= 201 status))
            (is (match? {:title "not bad"
                         :content "Today, I wanna show you ...."
                         :rate 4}
                        body))
            (is (clojure.string/includes? (get headers "location") (:id body)))
            (testing "path-paramsのproduct-idが登録されている"
              (is (= product1-id (:product-id body))))

            (testing "tokenのuser-id(sub)が登録されている"
              (is (= (-> user1-token auth/parse-token :sub) (:user-id body))))))

        (testing "tokenなしは401"
          (let [{:keys [status body]} (helper/http-post (str base-url "/api/products/" product1-id "/reviews")
                                                        {:title "awsome"
                                                         :content "tons of pros ..."
                                                         :rate 5})]
            (is (= 401 status))
            (is (= "login user only" (:error body)))))
        (testing "フィールドが足りない場合は 400"
          (let [{:keys [status body]} (helper/http-post (str base-url "/api/products/" product1-id "/reviews")
                                                        user1-token-header
                                                        {:title "not bad"
                                                         :rate 4})]
            (is (= 400 status))))
        (testing "フィールドの型が合わない場合は 400"
          (let [{:keys [status body]} (helper/http-post (str base-url "/api/products/" product1-id "/reviews")
                                                        user1-token-header
                                                        {:title "excellent"
                                                         :content ""
                                                         :rate "good!"})]
            (is (= 400 status))))
        (testing "ユーザーが存在しない 404"
          ;そもそもtokenが発行されないはず？ ;ログイン後にユーザー削除の可能性も？ 
          )
        (testing "product が存在しない 404"
          (let [{:keys [status body]} (helper/http-post (str base-url "/api/products/" "00000000-0000-0000-0000-000000000000" "/reviews")
                                                        user1-token-header
                                                        {:title "awsome"
                                                         :content "tons of pros ..."
                                                         :rate 5})]
            (is (= 404 status))
            (is (= "product not found" (:error body))))))

      (helper/http-post (str base-url "/api/products/" product1-id "/reviews")
                        user2-token-header
                        {:title "awsome"
                         :content "tons of pros ..."
                         :rate 5})
      #_(helper/http-post (str "/api/products/" product-id "/reviews")
                          other-token-header
                          {:title "awsome"
                           :content "tons of pros ..."
                           :rate 5})
      (helper/http-post (str base-url "/api/products/" product2-id "/reviews")
                        user1-token-header
                        {:title "lots of fun, but"
                         :content "to small for me"
                         :rate 3})

      (testing " get /products/:product-id/reviews"
        (testing "tokenなしでOK。その商品のレビューを配列で返す"
          (let [{:keys [status body] :as result} (helper/http-get (str base-url "/api/products/" product1-id "/reviews"))]
            (is (= 200 status))
            (is (coll? (:reviews body)))
            (is (= 2 (count (:reviews body))))

            (testing "投稿ユーザーも返す"
              (let [review (-> body :reviews first)]
                (is (string? (:user-name review)))))))

        (testing "商品がなければ 404"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/products/" "00000000-0000-0000-0000-000000000000" "/reviews"))]
            (is (= 404 status))
            (is (= "product not found" (:error body)))))

        (testing "レビューがなければ404"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/products/" product3-id "/reviews"))]
            (is (= 404 status))
            (is (= "reviews not found" (:error body))))))


      (testing " get /users/:user-id/reviews"
        (testing "tokenなしでOK。そのユーザーのレビューを配列で返す"
          (let [{:keys [status body] :as result} (helper/http-get (str base-url "/api/users/" user1-id "/reviews"))]
            (is (= 200 status))
            (is (coll? (:reviews body)))
            (is (= 2 (count (:reviews body))))

            (testing "商品名も返す"
              (let [review (-> body :reviews first)]
                (is (string? (:product-name review)))))))

        (testing "ユーザーがなければ 404"
          (let [{:keys [status body] :as result} (helper/http-get (str base-url "/api/users/" "00000000-0000-0000-0000-000000000000" "/reviews"))]
            (is (= 404 status))
            (is (= "user not found" (:error body)))))

        (testing "レビューがなければ404"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/users/" user3-id "/reviews"))]
            (is (= 404 status))
            (is (= "reviews not found" (:error body))))))

      (testing "get /reviews/:review-id"
        (testing "取得した内容を返す :review {,,,}"
          (let [{:keys [status body] :as result} (helper/http-get (str base-url "/api/reviews/" review-p1u1-id))
                review (:review body)]
            (is (= 200 status))
            (is (= "not bad" (:title review)))
            (is (= 4  (:rate review)))

            #_(testing "商品名も返す"
                (let [review (-> body :reviews first)]
                  (is (string? (:product-name review)))))))

        (testing "対象データが無ければ 404 で, を返す"
          (let [{:keys [status body] :as result} (helper/http-get (str base-url "/api/reviews/" "00000000-0000-0000-0000-000000000000"))]
            (is (= 404 status))
            (is (= "review not found" (:error body)))))

        (testing ":id がUUIDでなければ 400 とメッセージ"
          (let [{:keys [status body] :as result} (helper/http-get (str base-url "/api/reviews/" "12345"))]
            (is (= 400 status)))))

      (testing "get /reviews"
        (testing "データの配列を返す"
          (let [{:keys [status body] :as result} (helper/http-get (str base-url "/api/reviews"))]
            (is (= 200 status))
            (is (= 3 (-> body :reviews count)))))

        (testing "クエリパラメータで絞り込み"
          (testing "対象データが無ければ404で、:products nilを返す"))
        (testing "クエリパラメータでページネーション"
          (testing "offset,limit 数値以外は400"))
        (testing "クエリパラメータでソート"
          (testing "")
          (testing "orderのasc,desc以外は400")))

      (testing " update は無し"
         ;405
        )

      (testing "delete"
        (testing "認証なしは401"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/reviews/" review-p1u1-id))]
            (is (= 401 status))
            (is (= "login user only" (:error body)))))

        (testing "本人以外は消せない 403"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/reviews/" review-p1u1-id)
                                                          user2-token-header)]
            (is (= 403 status))
            (is (= "owner only" (:error body)))))

        #_(helper/http-delete (str "/api/reviews/" "00000000-0000-0000-0000-000000000000")
                              {"authorization" (str "Token " "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOm51bGwsIm5hbWUiOiJjaHJpcyIsImV4cCI6MTY4OTIzMjIzOH0.ke_IHSWje7T3HySP87PqZDDhHtXqI1KIhi1DUiuXVL8")})

        (testing "対象データが無ければ 404"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/reviews/" "00000000-0000-0000-0000-000000000000")
                                                          user1-token-header)]
            (is (= 404 status))
            (is (= "review not found" (:error body)))))

        (testing ":review-id  無しは405"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/reviews")
                                                          user1-token-header)]
            (is (= 405 status))))

        (testing ":review-idの型が違っていれば400"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/reviews/" "abc")
                                                       user1-token-header)]
            (is (= 400 status))))

        (testing "削除に成功したらno contentで何も返さない"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/reviews/" review-p1u1-id)
                                                          user1-token-header)]
            (is (= 204 status))
            (is (nil? body))))))))

