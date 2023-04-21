(ns isomorphic-clojure-webapp.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.boundary.users :as users]))

(defn- parse-l [v] ;とりあえず。coerce?
  (if (string? v)
    (Long/parseLong v)
    v))

(defmethod ig/init-key ::all [_ {:keys [db]}]
  (fn [req] 
    (let [result (users/get-users db)]
      (if (:errors result)
        {:status 404
         :body {:errors (:errors result)}}
        {:status 200
         :body result}))))

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
    (let [id (-> (get-in req [:path-params :id]) (parse-l))
          result (users/get-user-by-id db id)]
      (if (:errors result) 
        {:status 404
         :body {:errors (:errors result) }}
        {:status 200
         :body result}))))


(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [req]
    (let [id (-> (get-in req [:path-params :id]) parse-l)
          values (:body-params req) 
          {:keys [id errors] :as result} (users/update-user db id values)] 
      (if errors
        {:status 404
         :body {:errors errors}}
        {:status 200
         :body {:id id}}))))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [req]
    (let [id (-> (get-in req [:path-params :id]) parse-l)
          result (users/delete-user db id)]
      (if (:errors result) 
        {:status 404
         :body {:errors (:errors result)}}
        {:status 204
         :body {}}))))