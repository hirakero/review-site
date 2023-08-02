(ns isomorphic-clojure-webapp.api.handler.products
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.products :as products]
            [ring.util.http-response :as res]))

(defmethod ig/init-key ::list [_ {:keys [db]}]
  (fn [{:keys [query-params]}]
    (let [kw-key-params (zipmap (->> query-params keys (map keyword)) (vals query-params))  ;TODO もっとシンプルに
          kw-val-key  (filter #(contains? kw-key-params %) [:sort :order])         ;←このキーの値はkeywordにする
          kw-val-params (reduce #(update %1 %2 keyword) kw-key-params kw-val-key)
          result (products/get-products db kw-val-params)]
      (if (empty? result)
        (res/not-found {:error "products not found"})
        (res/ok {:products result})))))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (products/get-product-by db :id (:product-id path-params))]
      (if (empty? result)
        (res/not-found {:error "product not found"})
        (res/ok {:product result})))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [result (products/create-product db body-params)]
      (if (empty? result)
        (res/bad-request {:error ""})
        (res/created (str "/api/products/" (:id result))  result)))))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [{:keys [path-params body-params]}]
    (let [result (products/update-product db (:product-id path-params) body-params)]
      (if (empty? result)
        (res/not-found {:error "product not found"})
        (res/ok  result)))))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (products/delete-product db (:product-id path-params))]
      (if (empty? result)
        (res/not-found {:error "product not found"})
        (res/no-content)))))
