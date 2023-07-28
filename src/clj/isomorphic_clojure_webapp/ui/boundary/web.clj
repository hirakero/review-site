(ns isomorphic-clojure-webapp.ui.boundary.web
  (:require [integrant.core :as ig]))

(defrecord Boundary [base-url])

(defmethod ig/init-key ::host [_ {:keys [url]}]
  (->Boundary url))
