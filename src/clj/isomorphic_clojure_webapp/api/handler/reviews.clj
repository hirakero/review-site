(ns isomorphic-clojure-webapp.api.handler.reviews
  (:require [integrant.core :as ig]))

(defmethod ig/init-key ::list [_ {:keys [db]}]
  (fn [req]))
(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [req]))
(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [req]))
(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [req]))
(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [req]))
