(ns isomorphic-clojure-webapp.api.auth
  (:require [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.sign.jwt :as jwt]
            #_[buddy.sign.jws :as jws]
            [ring.util.response :as rres]))

(def secret-key "SECRET-KEY")
(def backend (backends/jws {:secret secret-key}))

(defn create-token [payload]
  (jwt/sign payload secret-key))

#_(defn wrap-jwt-authentication [handler]
    (wrap-authentication handler backend))

(comment
  "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiYWJjIn0.rd0u6j57sZpqSYLsMspacu23rX_McpRjbmOUO9jpYRU"
  "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiYWJjZWRmZSJ9.rd0u6j57sZpqSYLsMspacu23rX_McpRjbmOUO9jpYRU"
  "eyJ1c2VyIjoiYWJjZWRmZSJ9"


  (defn handler [req]

    {:status 200
     :body "hello"})
  (def app (-> handler
               (wrap-authentication backend)))
  (app {})


  (let [ts  (jwt/sign {:user "abc"} secret-key)
        st  (jwt/unsign ts secret-key)
        ss (jwt/unsign "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiYWJjZWRmZSJ9.eS6z7tM5wc4mrRyGstnrCDSy8C36021y3pGg1FFd8yM" secret-key)
        #_#_ss  (jws/sign {:user "abc"} secret-key)]
    [ss])

  (defn login-handler [req]
    (let [data (:form-params req)
          token (jwt/sign {:user 1} secret-key)])))

