(ns isomorphic-clojure-webapp.api.handler.products-test
  (:require [clojure.test :refer [deftest testing is are use-fixtures]]
            [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]
            [integrant.core :as ig]
            [integrant.repl.state :refer [system config]]
            [next.jdbc :as jdbc]
            [isomorphic-clojure-webapp.api.handler.products :as sut]))

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
  (testing "get /products データがない場合は何も返さない"
    (let [{:keys [status body] :as all} (helper/http-get "/api/products")]
      (is (=  404 status))
      (is (nil? body))))
  (let [{:keys [status headers body]} (helper/http-post "/api/products"
                                                        {:name "Hammer XT"
                                                         :description "for hammer grip"})
        id (:id body)]
    (testing "post /products 登録した内容を直接返す"
      (is (= status 201))
      (is (get headers "location"))
      (is (= "Hammer XT" (:name body)))
      (is (= "for hammer grip" (:description body))))

    (testing "get /products/:product-id "
      (testing "取得した内容を返す"
        (let [{:keys [status body]} (helper/http-get (str "/api/products/" id))]
          (is (= 200 status))
          (is (= "Hammer XT" (-> body :product :name)))))

      (testing "対象データが無ければ not found で何も返さない"
        (let [{:keys [status body]} (helper/http-get (str "/api/products/00000000-0000-0000-0000-000000000000"))]
          (is (= 404 status))
          (is (nil? body)))))

    (testing "put /products/:product-id"
      (testing "更新した内容を直接返す"
        (let [{:keys [status body]} (helper/http-put (str "/api/products/" id) {:name "Hammer LT"})]
          (is (= 200 status))
          (is (= "Hammer LT" (-> body :name)))))
      (testing "対象データが無ければ not found で何も返さない"
        (let [{:keys [status body]} (helper/http-put "/api/products/00000000-0000-0000-0000-000000000000" {:name "Hammer LT"})]
          (is (= 404 status))
          (is (nil? body)))))

    (testing "get /products データの配列を返す"
      (helper/http-post "/api/products"
                        {:name "axiom ocuralis"
                         :description "ocuralis plug"})
      (helper/http-post "/api/products"
                        {:name "Slant Roller"
                         :description "small"})
      (helper/http-post "/api/products"
                        {:name "Sparrow"
                         :description "thin profile"})
      (let [{:keys [status body]} (helper/http-get "/api/products")]
        (is (= 200 status))
        (is (vector? (-> body :products)))
        (is (= 4 (-> body :products count)))))

    (testing "クエリパラメータで絞り込み"
      (let [{:keys [status body]} (helper/http-get "/api/products?name=Sparrow")]
        (is (= 200 status))
        (is (= "Sparrow" (-> body :products first :name))))
      (testing "対象データが無ければ404で何も返さない"
        (let [{:keys [status body]} (helper/http-get "/api/products?name=Swarrow")]
          (is (= 404 status))
          (is (nil? body)))))

    (testing "クエリパラメータでページネーション"
      (let [{:keys [status body]} (helper/http-get "/api/products?offset=2")]
        (is (= 2 (-> body :products count))))
      (let [{:keys [status body]} (helper/http-get "/api/products?limit=3")]
        (is (= 3 (-> body :products count)))))

    (testing "delete"
      (testing "削除に成功したらno contentで何も返さない"
        (let [{:keys [status body]} (helper/http-delete (str "/api/products/" id))]
          (is (= 204 status))
          (is (nil? body))))
      (testing "対象データが無ければ not foundで何も返さない"
        (let [{:keys [status body]} (helper/http-delete "/api/products/00000000-0000-0000-0000-000000000000")]
          (is (= 404 status))
          (is (nil? body)))))))
