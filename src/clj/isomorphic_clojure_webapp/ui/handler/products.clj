(ns isomorphic-clojure-webapp.ui.handler.products
  (:require [integrant.core :as ig]
            [rum.core :as rum]))

(defn common [& body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-html [:html
                           [:body body]])})


(defmethod ig/init-key ::list [_ _]
  (fn [req]
    (common
     [:h2 "product list"]
     [:ul (for [i (range 5)]
            [:li i])])))

(defmethod ig/init-key ::detail [_ _]
  (fn [req]
    (common [:h2 "product fetch"])))

(defmethod ig/init-key ::create [_ _]
  (fn [req]
    (common [:h2 "product create"])))

(defmethod ig/init-key ::create-post [_ _]
  (fn [req]
    (common [:h2 "product create post"])))

(defmethod ig/init-key ::edit [_ _]
  (fn [req]
    (common [:h2 "product edit"])))

(defmethod ig/init-key ::edit-post [_ _]
  (fn [req]
    (common [:h2 "product edit post"])))

(defmethod ig/init-key ::delete [_ _]
  (fn [req]
    (common [:h2 "product delete"])))

(defmethod ig/init-key ::delete-post [_ _]
  (fn [req]
    (common [:h2 "product delete post"])))
