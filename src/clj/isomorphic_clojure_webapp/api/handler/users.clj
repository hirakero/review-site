(ns isomorphic-clojure-webapp.api.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.users :as users]))

(defmethod ig/init-key ::all [_ {:keys [db]}]
  (fn [req]
    (let [result (users/get-users db)]
      (if (empty? result)
        {:status 404
         :body {:users []}}
        {:status 200
         :body {:users result}}))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [result (users/create-user db body-params)]
      (if (empty? result)
        {:status 400
         :body {:user nil}}
        {:status 201
         :body {:user result}}))))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (users/get-user-by db :id (:user-id path-params))]
      (if (empty? result)
        {:status 404
         :body {:user nil}}
        {:status 200
         :body {:user result}}))))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [{:keys [path-params body-params]}]
    (let [result (users/update-user db (:user-id path-params) body-params)]
      (if (empty? result)
        {:status 404
         :body {:user nil}}
        {:status 200
         :body {:user result}}))))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (users/delete-user db (:user-id path-params))]
      (if (empty? result)
        {:status 404
         :body {}}
        {:status 204
         :body {}}))))

(defmethod ig/init-key ::login [_ {:keys [db]}]
  (fn [req]))

(defmethod ig/init-key ::signin [_ {:keys [db]}]
  (fn [req]))