(ns isomorphic-clojure-webapp.ui.response
  (:require [rum.core :as rum]))

(defn ok [& body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-html [:html
                           [:head
                            [:title "review site"]
                            [:link {:rel "stylesheet"
                                    :href "https://cdn.jsdelivr.net/gh/kognise/water.css@latest/dist/light.min.css"}]]
                           [:body body]])})

(defn see-other [location]
  {:status 303
   :headers {"content-type" "text/html"
             "Location" location}})