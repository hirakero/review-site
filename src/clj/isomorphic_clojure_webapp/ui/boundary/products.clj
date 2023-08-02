(ns isomorphic-clojure-webapp.ui.boundary.products
  (:require [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]
            [isomorphic-clojure-webapp.ui.boundary.web]))

(defprotocol Products

  (get-products [this query])

  (get-product-by-id [this id])

  (create-product [this values])

  (update-product [this id values])

  (delete-product [this id]))


(extend-protocol Products
  isomorphic_clojure_webapp.ui.boundary.web.Boundary

  (get-products [{:keys [base-url]} query]
    (let [query-string (->> (keys query)  (map #(str (name %) "=" (get query %))) (clojure.string/join "&"))]
      (helper/http-get (str base-url "/api/products" (when-not (empty? query-string) (str "?" query-string))))))

  (get-product-by-id [{:keys [base-url]} product-id]
    (helper/http-get (str base-url "/api/products/" product-id)))

  (create-product [{:keys [base-url]} values]
    (helper/http-post (str base-url "/api/products") values))

  (update-product [{:keys [base-url]} id values]
    (helper/http-put (str base-url "/api/products/" id) values))

  (delete-product [{:keys [base-url]} product-id]
    (helper/http-delete (str base-url "/api/products/" product-id))))
