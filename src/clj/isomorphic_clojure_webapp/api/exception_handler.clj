(ns isomorphic-clojure-webapp.api.exception-handler
  (:require [clj-http.client :as client]
            [ring.util.response :as res]
            [integrant.core :as ig]
            [reitit.ring.middleware.exception :as exception]
            [fipp.edn :as edn]
            [ring.mock.request :as mock])
  (:import [java.sql SQLException]))


(defn wrap-exception-handler [handler]
  (fn [req]
    (try
      (handler req)
      (catch clojure.lang.ExceptionInfo e
        (-> (res/status 500)
            (assoc :body {:error (str (str "ex info:" (ex-message e)))})))
      (catch Exception e
        (-> (res/status 500)
            (assoc :body {:error (str "unhandled error" (.getMessage e))}))))))

(defmethod ig/init-key ::wrap-exception-handler [_ _]
  wrap-exception-handler)

(defn tst [req]
  (try

    (throw (ex-info "ex-info " {}))
    #_((fn [_] (new java.sql.SQLException)) req)

    (catch java.sql.SQLException e
      (-> (res/status 500)
          (assoc :body {:error "sql error"})))
    #_(catch clojure.lang.ExceptionInfo e
        (-> (res/status 500)
            (assoc :body {:error (str (str "ex error"))})))
    (catch Exception e)))
(type (tst {}))

(comment
  (try
    ((fn [_] (throw (ex-info "ex-info " {}))) {})

    (catch SQLException e
      (-> (res/status 500)
          (assoc :body {:error (str "sql exception :"  (.getMessage e))})))
    (catch clojure.lang.ExceptionInfo e
      (-> (res/status 500)
          (assoc :body {:error (str (str "ex info:" (ex-message e)))})))
    (catch Exception e
      (-> (res/status 500)
          (assoc :body {:error (str "unhandled error" (.getMessage e))}))))

  (let [hnd (fn [req] {:status 200 :body "ok"})
        exin-hnd (fn [_] (throw (ex-info "ex-info " {})))
        ex-hnd (fn [_] (throw (new java.lang.Exception)))
        req (ring.mock.request/request :get "/api/health")]
    #_(handler req)
    #_(ex-handler req)

    #_(ex-hnd req)
    ((wrap-exception-handler
      exin-hnd)
     req))

  (try
    (/ 1 0)
    (catch Exception e (str "caught exception: " (.getMessage e)))))

