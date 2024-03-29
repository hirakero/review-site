(ns isomorphic-clojure-webapp.api.handler.products-test
  (:require [clojure.test :refer [deftest testing is are use-fixtures]]
            [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]
            [integrant.core :as ig]
            [integrant.repl.state :refer [system config]]
            [next.jdbc :as jdbc]))

(comment
  (let [boundary (:duct.database.sql/hikaricp system)
        handler (ig/init-key :isomorphic-clojure-webapp.handler.products/create boundary)]
    (handler {:request-method :post :body {:name "abc"}})))

(use-fixtures :each
  (fn [f]
    (let [boundary (:duct.database.sql/hikaricp system)
          ds (-> boundary :spec :datasource)]
      (jdbc/execute! ds ["delete from products"])
      #_(jdbc/execute! ds ["select * from products"])
      #_(jdbc/execute! ds ["insert into products (name, description) 
                          values ('hammer',''),
                                 ('bean flip',''),
                                 ('scout','')"]))
    (f)))

(deftest handler-products-test
  (let [base-url "http://localhost:3000"]
    (let [{:keys [status body]} (helper/http-get (str base-url "/api/products"))]
      (testing "get /products データがない場合は404"
        (is (=  404 status))
        (is (nil? (:products body)))
        (is (= "products not found" (:error body)))))
    (let [{:keys [status headers body]} (helper/http-post (str base-url "/api/products")
                                                          {:name "Hammer XT"
                                                           :description "for hammer grip"})
          id (:id body)]

      (testing "post /products"
        (testing " 登録した内容を直接返す"
          (is (= 201 status))
          (is (get headers "location"))
          (is (= "Hammer XT" (:name body)))
          (is (= "for hammer grip" (:description body))))

        (testing " 空bodyは 400"
          (let [{:keys [status body]} (helper/http-post (str base-url "/api/products")
                                                        {})]
            (is (= 400 status))
            (is (contains? body :spec))))
        (testing "フィールドの型が合わない場合は 400"
          (let [{:keys [status body]} (helper/http-post (str base-url "/api/products")
                                                        {:name 100
                                                         :description 200})]
            (is (= 400 status))
            (is (contains? body :spec))))
        (testing "POST /products フィールドが足りない場合は 400"
          (let [{:keys [status body]} (helper/http-post (str base-url "/api/products")
                                                        {:name "cleaver pro"})]
            (is (= 400 status))
            (is (contains? body :spec))))
        (testing "POST /products/:idは 405"
          (let [{:keys [status body]} (helper/http-post (str base-url "/api/products/" id)
                                                        {:name "Hammer LT"
                                                         :description "for hammer grip"})]
            (is (= 405 status))
            (is (= "method-not-allowed" (:error body))))))

      (testing "get /products/:product-id "
        (testing "取得した内容を返す"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/products/" id))]
            (is (= 200 status))
            (is (= "Hammer XT" (-> body :product :name)))))

        (testing "対象データが無ければ 404 "
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/products/00000000-0000-0000-0000-000000000000"))]
            (is (= 404 status))
            (is (nil? (:product body)))
            (is (= "product not found" (:error body)))))

        (testing ":id がUUIDでなければ 400 とメッセージ"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/products/123"))]
            (is (= 400 status))
            (is (contains? body :spec)))))

      (testing "put /products/:product-id"
        (testing "更新した内容を直接返す"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/products/" id) {:name "Hammer LT"})]
            (is (= 200 status))
            (is (= "Hammer LT" (-> body :name)))))
        (testing "対象データが無ければ404、 :error \"not found\" を返す"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/products/00000000-0000-0000-0000-000000000000")
                                                       {:name "Hammer LT"})]
            (is (= 404 status))
            (is (= "product not found" (:error body)))))
        (testing ":user-idの型違いは400"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/products/123")
                                                       {:name "Hammer LT"})]
            (is (= 400 status))
            (is (contains? body :spec))))
        (testing "bodyのフィールド名違いは400"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/products/" id)
                                                       {:title "Hammer LT"})]
            (is (= 400 status))
            (is (contains? body :spec))))

        (testing ":product-idなし405"
          (let [{:keys [status body]} (helper/http-put (str base-url "/api/products")
                                                       {:name "Hammer LT"})]
            (is (= 405 status))
            (is (= "method-not-allowed" (:error body))))))

      (testing "get /products"
        (helper/http-post (str base-url "/api/products")
                          {:name "axiom ocuralis"
                           :description "ocuralis plug"})
        (helper/http-post (str base-url "/api/products")
                          {:name "Slant Roller"
                           :description "small"})
        (helper/http-post (str base-url "/api/products")
                          {:name "Sparrow"
                           :description "thin profile"})
        (testing "データの配列を返す"
          (let [{:keys [status body]} (helper/http-get (str base-url "/api/products"))]
            (is (= 200 status))
            (is (vector? (-> body :products)))
            (is (= 4 (-> body :products count)))))

        (testing "クエリパラメータで絞り込み"
          (testing ""
            (let [{:keys [status body]} (helper/http-get (str base-url "/api/products?name=Sparrow"))]
              (is (= 200 status))
              (is (= "Sparrow" (-> body :products first :name)))))

          (testing "対象データが無ければ404"
            (let [{:keys [status body]} (helper/http-get (str base-url "/api/products?name=abc"))]
              (is (= 404 status))
              (is (nil? (:products body)))
              (is (= "products not found" (:error body))))))

        (testing "クエリパラメータでページネーション"
          (testing ""
            (let [{:keys [status body]} (helper/http-get (str base-url "/api/products?offset=2"))]
              (is (= 2 (-> body :products count))))
            (let [{:keys [status body]} (helper/http-get (str base-url "/api/products?limit=3"))]
              (is (= 3 (-> body :products count)))))
          (testing "offset,limit 数値以外は400"
            (let [{:keys [status body]} (helper/http-get (str base-url "/api/products?offset=a&limit=b"))]
              (is (= 400 status)))))

        (testing "クエリパラメータでソート"
          (testing ""
            (let [{:keys [status body]} (helper/http-get (str base-url "/api/products?sort=name&order=desc"))]
              (is (= ["Sparrow" "Slant Roller" "Hammer LT" "axiom ocuralis"] (->> body :products (map #(:name %))))))
            (let [{:keys [status body]} (helper/http-get (str base-url "/api/products?sort=name"))]
              (is (= ["axiom ocuralis" "Hammer LT" "Slant Roller" "Sparrow"] (->> body :products (map #(:name %)))))))
          (testing "orderのasc,desc以外は400"
            (let [{:keys [status body]} (helper/http-get (str base-url "/api/products?sort=name&order=abc"))]
              (is (= 400 status))))))

      (testing "delete"
        (testing "削除に成功したらno contentで何も返さない"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/products/" id))]
            (is (= 204 status))
            (is (nil? body))))

        (testing "対象データが無ければ 404で、:error \"not found\" を返す"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/products/00000000-0000-0000-0000-000000000000"))]
            (is (= 404 status))
            (is (= "product not found" (:error body)))))

        (testing ":product-idの型が違っていれば400"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/products/123"))]
            (is (= 400 status))
            (is (contains? body :spec))))
        (testing ":product-id なしは405"
          (let [{:keys [status body]} (helper/http-delete (str base-url "/api/products"))]
            (is (= 405 status))
            (is (= "method-not-allowed" (:error body)))))))))
