(ns isomorphic-clojure-webapp.ui.boundary.products
  (:require [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]))

(defprotocol Products

  (get-products [query])

  (get-product-by-id [id])

  (create-product [values])

  (update-product [id values])

  (delete-product [id]))



(extend-protocol Products
  java.lang.Object
  (get-products [query]
    (let [query-string (->> (keys query)  (map #(str (name %) "=" (get query %))) (clojure.string/join "&"))
          {:keys [status body]}  (helper/http-get (str "/api/products" (when-not (empty? query-string) (str "?" query-string))))]
      {:status status :body body}))

  (get-product-by-id [product-id]
    (let [{:keys [status body]}  (helper/http-get (str "/api/products/" product-id))]
      {:status status :body body}))

  (create-product [{:keys [name description]}]
    (let [{:keys [status body]} (helper/http-post "/api/products"
                                                  {:name name
                                                   :description description})]
      {:status status :body body}))

  (update-product [id values]
    (let [{:keys [status body]} (helper/http-put (str "/api/products/" id) values)]
      {:status status :body body}))

  (delete-product [product-id]
    (let [{:keys [status body]} (helper/http-delete (str "/api/products/" product-id))]
      {:status status :body body})))

