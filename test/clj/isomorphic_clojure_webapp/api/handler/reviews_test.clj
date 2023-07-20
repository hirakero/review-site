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
  (let [;正規ユーザー追加
        {{user1-id :id} :body}  (helper/http-post "/api/signup"
                                                  {:name "Chris"
                                                   :email "chris@email.com"
                                                   :password "password"})
               ;サインイン、トークン保持
        {{user1-token :token} :body} (helper/http-post "/api/signin"
                                                       {:name "Chris"
                                                        :email "chris@email.com"
                                                        :password "password"})
        user1-token-header {"authorization" (str "Token " user1-token)}



               ;商品追加
        {{product1-id :id} :body} (helper/http-post "/api/products"
                                                    {:name "sr400"
                                                     :description "single 400cc"})


               ;レビュー追加
        result (helper/http-post (str "/api/products/" product1-id "/reviews")
                                 user1-token-header
                                 {:title "not bad"
                                  :content "Today, I wanna show you ...."
                                  :rate 4})
        #_#_p1u1-review-id (-> result :body :id)]
    [user1-id user1-token product1-id result]))

(deftest handler-reviews-test
  (let [;正規ユーザー追加
        {{user1-id :id} :body} (helper/http-post "/api/signup"
                                                 {:name "Chris"
                                                  :email "chris@email.com"
                                                  :password "password"})
        ;サインイン、トークン保持
        {{user1-token :token} :body} (helper/http-post "/api/signin"
                                                       {:name "Chris"
                                                        :email "chris@email.com"
                                                        :password "password"})
        user1-token-header {"authorization" (str "Token " user1-token)}

        ;他のユーザー追加
        _  (helper/http-post "/api/signup"
                             {:name "Dave"
                              :email "dave@email.com"
                              :password "password"})

        {{user2-token :token} :body} (helper/http-post "/api/signin"
                                                       {:name "Dave"
                                                        :email "dave@email.com"
                                                        :password "password"})

        user2-token-header {"authorization" (str "Token " user2-token)}

        {{user3-id :id} :body} (helper/http-post "/api/signin"
                                                 {:name "Eric"
                                                  :email "eric@email.com"
                                                  :password "password"})

        ;商品追加
        {{product1-id :id} :body} (helper/http-post "/api/products"
                                                    {:name "sr400"
                                                     :description "single 400cc"})

        {{product2-id :id} :body} (helper/http-post "/api/products"
                                                    {:name "monkey"
                                                     :description "single 50cc"})

        {{product3-id :id} :body} (helper/http-post "/api/products"
                                                    {:name "dax 125"
                                                     :description "single 125cc"})
        ;レビュー追加
        result (helper/http-post (str "/api/products/" product1-id "/reviews")
                                 user1-token-header
                                 {:title "not bad"
                                  :content "Today, I wanna show you ...."
                                  :rate 4})
        p1u1-review-id (-> result :body :id)]

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
        (let [{:keys [status body]} (helper/http-post (str "/api/products/" product1-id "/reviews")
                                                      {:title "awsome"
                                                       :content "tons of pros ..."
                                                       :rate 5})]
          (is (= 401 status))))
      (testing "フィールドが足りない場合は 400"
        (let [{:keys [status body]} (helper/http-post (str "/api/products/" product1-id "/reviews")
                                                      user1-token-header
                                                      {:title "not bad"
                                                       :rate 4})]
          (is (= 400 status))))
      (testing "フィールドの型が合わない場合は 400"
        (let [{:keys [status body]} (helper/http-post (str "/api/products/" product1-id "/reviews")
                                                      user1-token-header
                                                      {:title "excellent"
                                                       :content ""
                                                       :rate "good!"})]
          (is (= 400 status))))
      (testing "ユーザーが存在しない 404?"
          ;そもそもtokenが発行されないはず？ ;ログイン後にユーザー削除の可能性も？
        )
      (testing "product が存在しない 404?"
        (let [{:keys [status body]} (helper/http-post (str "/api/products/" "00000000-0000-0000-0000-000000000000" "/reviews")
                                                      user1-token-header
                                                      {:title "awsome"
                                                       :content "tons of pros ..."
                                                       :rate 5})]
          (is (= 404 status)))))

    (helper/http-post (str "/api/products/" product1-id "/reviews")
                      user2-token-header
                      {:title "awsome"
                       :content "tons of pros ..."
                       :rate 5})
    #_(helper/http-post (str "/api/products/" product-id "/reviews")
                        other-token-header
                        {:title "awsome"
                         :content "tons of pros ..."
                         :rate 5})
    (helper/http-post (str "/api/products/" product2-id "/reviews")
                      user1-token-header
                      {:title "lots of fun, but"
                       :content "to small for me"
                       :rate 3})

    (comment
      (helper/http-get (str "/api/products/aa68d2ee-5397-4d53-9b64-9d9c040d0953" "/reviews")))

    (testing " get /products/:product-id/reviews"
      (testing "tokenなしでOK。その商品のレビューを配列で返す"
        (let [{:keys [status body] :as result} (helper/http-get (str "/api/products/" product1-id "/reviews"))]
          (is (= 200 status))
          (is (coll? (:reviews body)))
          (is (= 2 (count (:reviews body))))

          (testing "投稿ユーザー名も返す"
            (let [review (-> body :reviews first)]
              (is (string? (:user-name review)))))))

      (testing "レビューがなければ404"
        (let [{:keys [status body]} (helper/http-get (str "/api/products/" product3-id "/reviews"))]
          (is (= 404 status))))

      (testing "商品がなければ 404"
        (let [{:keys [status body]} (helper/http-get (str "/api/products/" "00000000-0000-0000-0000-000000000000" "/reviews"))]
          (is (= 404 status)))))

    #_(testing " get /users/:user-id/reviews"
        (testing "tokenなしでOK。そのユーザーのレビューを配列で返す"
          (let [{:keys [status body] :as result} (helper/http-get (str "/api/users/" user1-id "/reviews"))]
            (is (= 200 status))
            #_(is (coll? (:reviews body)))
            #_(is (= 2 (count (:reviews body))))

            #_(testing "商品名も返す"
                (let [review (-> body :reviews first)]
                  (is (string? (:product-name review)))))))

        #_(testing "レビューがなければ404"
            (let [{:keys [status body]} (helper/http-get (str "/api/users/" user3-id "/reviews"))]
              (is (= 404 status))))

        #_(testing "ユーザーがなければ 404"
            (let [{:keys [status body]} (helper/http-get (str "/api/users/" "00000000-0000-0000-0000-000000000000" "/reviews"))]
              (is (= 404 status)))))

    (testing " get /reviews query")

    (testing "get /reviews/:review-id"
      (testing "取得した内容を返す :review {,,,}")
      (testing "対象データが無ければ 404 で, :review nilを返す")
      (testing ":id がUUIDでなければ 400 とメッセージ"))

    (testing "get /reviews"
      (testing "データの配列を返す")
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
        (let [{:keys [status body]} (helper/http-delete (str "/api/reviews/" p1u1-review-id))]
          (is (= 401 status))))

      (testing "本人以外は消せない 403"
        (let [{:keys [status body]} (helper/http-delete (str "/api/reviews/" p1u1-review-id)
                                                        user2-token-header)]
          (is (= 403 status))))

      #_(helper/http-delete (str "/api/reviews/" "00000000-0000-0000-0000-000000000000")
                            {"authorization" (str "Token " "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOm51bGwsIm5hbWUiOiJjaHJpcyIsImV4cCI6MTY4OTIzMjIzOH0.ke_IHSWje7T3HySP87PqZDDhHtXqI1KIhi1DUiuXVL8")})
      (comment
        (mock/request :get "")

        (mock/request :delete "/")

        (let [handler (ig/init-key :isomorphic-clojure-webapp.api.handler.reviews/delete (:duct.database.sql/hikaricp system))
              result (handler {:request-method :get
                               :uri "/api/health"})
              #_(handler (mock/request :delete
                                       "/api/reviews/00000000-0000-0000-0000-000000000000"))]
          result))

      (testing "対象データが無ければ 404"
        (let [{:keys [status body]} (helper/http-delete (str "/api/reviews/" "00000000-0000-0000-0000-000000000000")
                                                        user1-token-header)]
          (is (= 404 status))))

      (testing ":review-id  無しは405"
        (let [{:keys [status body]} (helper/http-delete "/api/reviews"
                                                        user1-token-header)]
          (is (= 405 status))))

      (testing ":review-idの型が違っていれば400"
        (let [{:keys [status body]} (helper/http-get (str "/api/reviews/" "abc")
                                                     user1-token-header)]
          (is (= 400 status))))

      (testing "削除に成功したらno contentで何も返さない"
        (let [{:keys [status body]} (helper/http-delete (str "/api/reviews/" p1u1-review-id)
                                                        user1-token-header)]
          (is (= 204 status))
          (is (nil? body)))))
    result))

