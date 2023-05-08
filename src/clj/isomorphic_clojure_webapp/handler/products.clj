(ns isomorphic-clojure-webapp.handler.products
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.boundary.products :as products]))

(defmethod ig/init-key ::all [_ {:keys [db]}]
  (fn [req]
    (let [result (products/get-products db {})]
      (if (empty? result)
        {:status 404
         :body {:products []}}
        {:status 200
         :body {:products result}}))))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (products/get-product-by db :id (:product-id path-params))]
      (if (empty? result)
        {:status 404
         :body {:product nil}}
        {:status 200
         :body {:product result}}))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [result (products/create-product db body-params)]
      (if (empty? result)
        {:status 400
         :body {:product nil}}
        {:status 201
         :body {:product result}}))))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [{:keys [path-params body-params]}]
    (let [result (products/update-product db (:product-id path-params) body-params)]
      (if (empty? result)
        {:status 404
         :body {:product nil}}
        {:status 200
         :body {:product result}}))))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (products/delete-product db (:product-id path-params))]
      (if (empty? result)
        {:status 404
         :body {}}
        {:status 204
         :body {}}))))
