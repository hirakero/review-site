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


(defmethod ig/init-key ::health [_a _b]
  (fn [req]
    {:status 200
     :headers {"content-type" "text/json"}
     :body {:message "running!"
            :token (:identity req)}}))
