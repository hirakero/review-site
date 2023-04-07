(ns isomorphic-clojure-webapp.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.boudary.users :as users]))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [req]
    (let [{:keys [user-id]} (users/create-user db {})]
      {:status 201
       :body {:user-id user-id}})))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [req] 
    (let [id (get-in req [:path-params :id])]
      (if-let [user (users/get-user-by-id db id)]
        {:status 200
         :body user}
        {:status 404}))))
