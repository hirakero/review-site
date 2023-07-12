(ns isomorphic-clojure-webapp.api.auth
  (:require [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.sign.jwt :as jwt]
            [buddy.auth :as buddy]
            #_[buddy.sign.jws :as jws]
            [ring.util.response :as rres]
            [integrant.core :as ig]))

(def secret-key "SECRET-KEY") ;TODO  環境変数から
(def exp-second 3600)
(def backend (backends/jws {:secret secret-key}))

(defn create-token
  "ユーザー情報のマップを受け取り、その情報をもとにjwsを返す"
  [{:keys [id name]}]
  (jwt/sign
   {:sub id
    :name name
    :exp (-> (System/currentTimeMillis)
             (quot  1000)
             (+ exp-second))}
   secret-key))

(defmethod ig/init-key ::wrap-user-only [key _]
  (fn [handler]
    (fn [req]
      (if-not (buddy/authenticated? req)
        {:status 401 :body {:error "unaudorized"}}
        (handler req)))))

#_(defn wrap-jwt-authentication [handler]
    (wrap-authentication handler backend))



