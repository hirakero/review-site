(ns isomorphic-clojure-webapp.api.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [ring.util.response :as rres]))

(defmethod ig/init-key ::all [_ {:keys [db]}]
  (fn [req]
    (let [result (users/get-users db)]
      (if (empty? result)
        (rres/not-found nil)
        (rres/response {:users result})))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [result (users/create-user db body-params)]
      (if (empty? result)
        (rres/bad-request {:user nil})
        (rres/created (str "/api/users/" (:id result)) result)))))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (users/get-user-by db :id (:user-id path-params))]
      (if (empty? result)
        (rres/not-found nil)
        (rres/response {:user result})))))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [{:keys [path-params body-params]}]
    (let [result (users/update-user db (:user-id path-params) body-params)]
      (if (empty? result)
        (rres/not-found nil)
        (rres/response  result)))))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (users/delete-user db (:user-id path-params))]
      (if (empty? result)
        (rres/not-found nil)
        {:status 204}))))

(defmethod ig/init-key ::signin [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [{:keys [username email]} body-params]
      ;TODO 入力内容のバリデーション spec?
      (let [user-exists? (users/get-user-by db :name username)
            email-exists? (users/get-user-by db :email email)]
        (def *user user-exists?)
        (def *email email-exists?)
        (if (or user-exists? email-exists?)
          {:status 400
           :body {:error-message "already exists"}}
          (let [result (users/create-user db body-params)]
            {:status 201
             :body {:user result}}))))))


(defmethod ig/init-key ::login [_ {:keys [db]}]
  (fn [{:keys [body-params]}]))
