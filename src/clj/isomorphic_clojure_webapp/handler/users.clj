(ns isomorphic-clojure-webapp.handler.users
  (:require [integrant.core :as ig]))

(defmethod ig/init-key ::create [_ _]
  (fn [req]
    {:status 201
     :body {:user-id 1}}))

(defmethod ig/init-key ::fetch [_ _]
  (fn [req]
    {:status 200
     :body {:id 1
            :name "Alice"}}))
