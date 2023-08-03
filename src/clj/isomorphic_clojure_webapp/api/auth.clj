(ns isomorphic-clojure-webapp.api.auth
  (:require [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.sign.jwt :as jwt]
            [buddy.auth :as buddy]
            [integrant.core :as ig]
            [ring.util.http-response :as res]))

(def secret-key (duct.core.env/env "SECRET_KEY"))
(def exp-second 3600)

(defn create-token
  "ユーザー情報のマップを受け取り、:expを追加して、jwsを返す"
  [{:keys [id name]}]
  (jwt/sign
   {:sub id
    :name name
    :exp (-> (System/currentTimeMillis)
             (quot  1000)
             (+ exp-second))}
   secret-key))

(defn parse-token
  "JWTをマップに戻す"
  [token]
  (jwt/unsign token secret-key))

(defmethod ig/init-key ::wrap-user-only [_ _]
  (fn [handler]
    (fn [req]
      (if-not (buddy/authenticated? req)
        (res/unauthorized {:error "login user only"})
        (handler req)))))

(defmethod  ig/init-key ::wrap-admin-only [_ _]
  (fn [handler]
    (fn [req]
      (let [role (some-> req
                         :identity
                         :role)]
        (if (= role :admin)
          (handler req)
          (res/unauthorized {:error "admin only"}))))))

;wrap-users-owner-only
;wrap-reviews-owner-only
