(ns isomorphic-clojure-webapp.api.handler.products-test
  (:require [clojure.test :refer [deftest testing is are use-fixtures]]
            [isomorphic-clojure-webapp.test-helper :as helper]
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
  (testing "get /products データがない場合は空のベクタを返す"
    (let [{:keys [status body] :as all} (helper/http-get "/api/products")]
      (is (=  404 status))
      (is (=  [] (:products body)))))
  (let [{:keys [status headers body]} (helper/http-post "/api/products"
                                                        {:name "Hammer XT"
                                                         :description "for hammer grip"})
        product (:product body)
        id (:id product)]
    (testing "post /products 登録した内容を返す"
      (is (= status 201))
      (is (get headers "location"))
      (is (= "Hammer XT" (:name product)))
      (is (= "for hammer grip" (:description product))))

    (testing "get /products/:product-id "
      (testing "取得した内容を返す"
        (let [{:keys [status body] :as all} (helper/http-get (str "/api/products/" id))]
          (is (= 200 status))
          (is (= "Hammer XT" (-> body :product :name)))))

      (testing "対象データが無ければ not found"
        (let [{:keys [status body] :as all} (helper/http-get (str "/api/products/00000000-0000-0000-0000-000000000000"))]
          (is (= 404 status)))))

    (testing "put /products/:product-id"
      (testing "更新した内容を返す"
        (let [{:keys [status body]} (helper/http-put (str "/api/products/" id) {:name "Hammer LT"})]
          (is (= 200 status))
          (is (= "Hammer LT" (-> body :product :name)))))
      (testing "対象データが無ければ not found"
        (let [{:keys [status body]} (helper/http-put "/api/products/00000000-0000-0000-0000-000000000000" {:name "Hammer LT"})]
          (is (= 404 status)))))

    (testing "get /products データの配列を返す"
      (helper/http-post "/api/products"
                        {:name "axiom ocuralis"
                         :description "ocuralis plug"})
      (let [{:keys [status body]} (helper/http-get "/api/products")]
        (is (= 200 status))
        (is (= 2 (-> body :products count)))))

    (testing "delete"
      (testing "削除に成功したらno content"
        (let [{:keys [status body]} (helper/http-delete (str "/api/products/" id))]
          (is (= 204 status))))
      (testing "対象データが無ければ not found"
        (let [{:keys [status body]} (helper/http-delete "/api/products/00000000-0000-0000-0000-000000000000")]
          (is (= 404 status)))))))
