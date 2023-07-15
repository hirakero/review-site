(ns isomorphic-clojure-webapp.api.handler.reviews-test
  (:require [clojure.test :refer [deftest testing is are use-fixtures]]
            [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]
            [integrant.core :as ig]
            [integrant.repl.state :refer [system config]]
            [next.jdbc :as jdbc]
            [matcher-combinators.clj-test]
            [isomorphic-clojure-webapp.api.auth :as auth]))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from reviews"]))
    (f)))

(deftest handler-reviews-test
  (println "\nhandler reviews test")
  (let [;正規ユーザー追加
        _  (helper/http-post "/api/signup"
                             {:name "Chris"
                              :email "chris@email.com"
                              :password "password"})
        ;サインイン、トークン保持
        {{:keys [token]} :body} (helper/http-post "/api/signin"
                                                  {:name "Chris"
                                                   :email "chris@email.com"
                                                   :password "password"})
        token-header {"authorization" (str "Token " token)}

        ;他のユーザー追加
        _  (helper/http-post "/api/signup"
                             {:name "Dave"
                              :email "dave@email.com"
                              :password "password"})

        {{other-token :token} :body} (helper/http-post "/api/signin"
                                                       {:name "Dave"
                                                        :email "dave@email.com"
                                                        :password "password"})

        other-token-header {"authorization" (str "Token " other-token)}

        ;商品追加
        {{product-id :id} :body} (helper/http-post "/api/products"
                                                   {:name "sr400"
                                                    :description "single 400cc"})
        result (helper/http-post (str "/api/products/" product-id "/reviews")
                                 token-header
                                 {:title "not bad"
                                  :content "Today, I wanna show you ...."
                                  :rate 4})
        review-id (-> result :body :id)]
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
            (is (= product-id (:product-id body))))

          (testing "tokenのuser-id(sub)が登録されている"
            (is (= (-> token auth/parse-token :sub) (:user-id body))))))

      (testing "tokenなしは401"
        (let [{:keys [status body]} (helper/http-post (str "/api/products/" product-id "/reviews")
                                                      {:title "awsome"
                                                       :content "tons of pros ..."
                                                       :rate 5})]
          (is (= 401 status))))
      (testing "フィールドが足りない場合は 400"
        (let [{:keys [status body]} (helper/http-post (str "/api/products/" product-id "/reviews")
                                                      token-header
                                                      {:title "not bad"
                                                       :rate 4})]
          (is (= 400 status))))
      (testing "フィールドの型が合わない場合は 400"
        (let [{:keys [status body]} (helper/http-post (str "/api/products/" product-id "/reviews")
                                                      token-header
                                                      {:title "excellent"
                                                       :content ""
                                                       :rate "good!"})]
          (is (= 400 status))))
      (testing "ユーザーが存在しない 404?"
          ;そもそもtokenが発行されないはず？
          ;ログイン後にユーザー削除の可能性も？
        )
      (testing "product が存在しない 404?"
        (let [{:keys [status body]} (helper/http-post (str "/api/products/" "00000000-0000-0000-0000-000000000000" "/reviews")
                                                      token-header
                                                      {:title "awsome"
                                                       :content "tons of pros ..."
                                                       :rate 5})]
          (is (= 404 status)))))

    (testing " get /products/:product-id/reviews")
    (testing " get /users/:user-id/reviews")
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
        (let [{:keys [status body]} (helper/http-delete (str "/api/reviews/" review-id))]
          (is (= 401 status))))

      (testing "本人以外は消せない 403"
        (let [{:keys [status body]} (helper/http-delete (str "/api/reviews/" review-id)
                                                        other-token-header)]
          (is (= 403 status))))

      #_(helper/http-delete (str "/api/reviews/" "00000000-0000-0000-0000-000000000000")
                            {"authorization" (str "Token " "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOm51bGwsIm5hbWUiOiJjaHJpcyIsImV4cCI6MTY4OTIzMjIzOH0.ke_IHSWje7T3HySP87PqZDDhHtXqI1KIhi1DUiuXVL8")})

      (testing "対象データが無ければ 404"
        (let [{:keys [status body]} (helper/http-delete (str "/api/reviews/" "00000000-0000-0000-0000-000000000000")
                                                        token-header)]
          (is (= 404 status))))

      (testing ":review-id  無しは405"
        (let [{:keys [status body]} (helper/http-delete "/api/reviews"
                                                        token-header)]
          (is (= 405 status))))

      (testing ":review-idの型が違っていれば400"
        (let [{:keys [status body]} (helper/http-get (str "/api/reviews/" "abc")
                                                     token-header)]
          (is (= 400 status))))

      (testing "削除に成功したらno contentで何も返さない"
        (let [{:keys [status body]} (helper/http-delete (str "/api/reviews/" review-id)
                                                        token-header)]
          (is (= 204 status))
          (is (nil? body)))))))