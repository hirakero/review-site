(ns isomorphic-clojure-webapp.api.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [ring.util.response :as rres]
            [isomorphic-clojure-webapp.api.auth :as auth]))

(defmethod ig/init-key ::list [_ {:keys [db]}]
  (fn [req]
    (let [result (users/get-users db)]
      (if (empty? result)
        (rres/not-found nil)
        (rres/response {:users result})))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [{:keys [name email]} body-params]
      (let [user-exists? (users/get-user-by db :name name)
            email-exists? (users/get-user-by db :email email)]
        (if (or user-exists? email-exists?)
          {:status 409
           :body {:error "already exists"}}
          (if-let [result (users/create-user db body-params)]
            (let [token (auth/create-token result)
                  result (assoc result :token token)]
              (rres/created (str "/api/users/" (:id result))
                            result))
            {:status 500
             :body {:error ""}}))))))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (users/get-user-by db :id (:user-id path-params))]
      (if (empty? result)
        (rres/not-found nil)
        (rres/response {:user result})))))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [{:keys [path-params body-params identity]}]
    (let [path-user-id (:user-id path-params)
          token-user-id (:sub identity)]
      (if (= path-user-id token-user-id)
        (let [result (users/update-user db path-user-id body-params)]
          (if (empty? result)
            (rres/not-found nil)
            (rres/response result)))
        (rres/status 403)))))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params identity]}]
    (let [token-user-id (:sub identity)
          path-user-id (:user-id path-params)]
      (if (= path-user-id token-user-id)
        (let [result (users/delete-user db path-user-id)]
          (if (empty? result)
            (rres/not-found nil)
            (rres/status 204)))
        (rres/status 403)))))


(defmethod ig/init-key ::signin [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [{:keys [name email password]} body-params]
      (if-let [result (users/signin db body-params)]
        (let [token (auth/create-token result)]
          (rres/response {:user result
                          :token token}))
        {:status 401
         :body {:error "signin failed"}}))))

