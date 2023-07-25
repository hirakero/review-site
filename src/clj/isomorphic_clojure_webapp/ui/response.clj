(ns isomorphic-clojure-webapp.ui.response
  (:require [rum.core :as rum]))

(defn ok [& body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-html [:html
                           [:head
                            [:title "review site"]
                            [:meta {:name "viewport"
                                    :content "width=device-width, initial-scale=1"}]
                            [:link {:rel "stylesheet"
                                    :href "https://cdn.jsdelivr.net/npm/purecss@3.0.0/build/pure-min.css"
                                    :integrity "sha384-X38yfunGUhNzHpBaEBsWLO+A0HDYOQi8ufWDkZ0k9e0eXz/tH3II7uKZ9msv++Ls"
                                    :crossorigin "anonymous"}]]
                           [:body body]])})

(defn see-other [location]
  {:status 303
   :headers {"content-type" "text/html"
             "Location" location}})