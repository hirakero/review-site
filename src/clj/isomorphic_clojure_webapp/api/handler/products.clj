(ns isomorphic-clojure-webapp.api.handler.products
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.products :as products]
            [ring.util.response :as rres]))

(defmethod ig/init-key ::all [_ {:keys [db]}]
  (fn [req]
    (let [result (products/get-products db {})]
      (if (empty? result)
        (rres/not-found {:products []})
        (rres/response {:products result})))))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (products/get-product-by db :id (:product-id path-params))]
      (if (empty? result)
        (rres/not-found {:product nil})
        (rres/response {:product result})))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [result (products/create-product db body-params)]
      (if (empty? result)
        (rres/bad-request {:product nil})
        (rres/created (str "/api/products/" (:id result)) {:product result})))))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [{:keys [path-params body-params]}]
    (let [result (products/update-product db (:product-id path-params) body-params)]
      (if (empty? result)
        (rres/not-found {:product nil})
        (rres/response {:product result})))))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (products/delete-product db (:product-id path-params))]
      (if (empty? result)
        (rres/not-found {})
        {:status 204
         :body {}}))))
