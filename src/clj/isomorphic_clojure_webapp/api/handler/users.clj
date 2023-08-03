(ns isomorphic-clojure-webapp.api.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [ring.util.http-response :as res]
            [isomorphic-clojure-webapp.api.auth :as auth]))

(defmethod ig/init-key ::list [_ {:keys [db]}]
  (fn [req]
    (let [result (users/get-users db)]
      (if (empty? result)
        (res/not-found! {:error "users not found"})
        (res/ok {:users result})))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [{:keys [name email]} body-params]
      (let [user-exists? (users/get-user-by db :name name)
            email-exists? (users/get-user-by db :email email)]
        (if (or user-exists? email-exists?)
          (res/conflict! {:error "user already exists"})
          (if-let [result (users/create-user db body-params)]
            (let [token (auth/create-token result)
                  result (assoc result :token token)]
              (res/created (str "/api/users/" (:id result))
                           result))
            (res/internal-server-error! {:error ""})))))))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [result (users/get-user-by db :id (:user-id path-params))]
      (if (empty? result)
        (res/not-found! {:error "user not found"})
        (res/ok {:user result})))))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [{:keys [path-params body-params identity]}]
    (let [path-user-id (:user-id path-params)
          token-user-id (:sub identity)]
      (if (= path-user-id token-user-id)
        (let [result (users/update-user db path-user-id body-params)]
          (if (empty? result)
            (res/not-found! {:error "user not found"})
            (res/ok result)))
        (res/forbidden! {:error "owner only"})))))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params identity]}]
    (let [token-user-id (:sub identity)
          path-user-id (:user-id path-params)]
      (if (= path-user-id token-user-id)
        (let [result (users/delete-user db path-user-id)]
          (if (empty? result)
            (res/not-found! {:error "user not found"})
            (res/no-content)))
        (res/forbidden! {:error "owner only"})))))


(defmethod ig/init-key ::signin [_ {:keys [db]}]
  (fn [{:keys [body-params]}]
    (let [{:keys [name email password]} body-params]
      (if-let [result (users/signin db body-params)]
        (let [token (auth/create-token result)]
          (res/ok {:user result
                   :token token}))
        (res/unauthorized {:error "signin failed"})))))

