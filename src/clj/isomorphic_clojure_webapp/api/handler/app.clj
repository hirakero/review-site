(ns isomorphic-clojure-webapp.api.handler.app
  (:require [integrant.core :as ig]
            [rum.core :refer [render-html]]))


(defmethod ig/init-key ::index [_ _]
  (fn [req]
    (prn req)
    {:status 200
     :headers {"content-type" "text/html"}
     :body (render-html [:html
                         [:body
                          [:#app]
                          [:script {:src "/js/main.js"}]]])}))


(defmethod ig/init-key ::health [_ _]
  (fn [req]
    {:status 200
     :headers {"content-type" "text/json"}
     :body {:message "running!"
            :token (:identity req)}}))

(defmethod ig/init-key ::exception [_ _]
  (fn [req]
    (throw (ex-info "ex info" {:error "error test"}))))
