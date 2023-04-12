(ns isomorphic-clojure-webapp.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.boundary.users :as users]))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [req]
    (let [{:keys [id errors] :as result} (users/create-user db (:body req))] 
      (if errors 
        {:status 400
         :body {:errors errors}}
        {:status 201
         :body {:id id}}))))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [req] 
    (let [id (get-in req [:path-params :id])
          result (users/get-user-by-id db id)]
      (if (:errors result) 
        {:status 404
         :body {:errors (:errors result) }}
        {:status 200
         :body result}))))
