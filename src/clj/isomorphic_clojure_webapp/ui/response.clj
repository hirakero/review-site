(ns isomorphic-clojure-webapp.ui.response
  (:require [rum.core :as rum]))

(defn ok [& body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-html [:html
                           [:head
                            [:meta {:charset "utf-8"}]
                            [:title "review site"]
                            [:meta {:name "viewport"
                                    :content "width=device-width, initial-scale=1"}]
                            [:script {:src "https://use.fontawesome.com/releases/v5.3.1/js/all.js"
                                      :defer "defer"}]
                            [:link {:rel "stylesheet"
                                    :href "https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css"}]]
                           [:body
                            [:section.section
                             [:div.container
                              body]]]])})
