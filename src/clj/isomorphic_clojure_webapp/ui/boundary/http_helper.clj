(ns isomorphic-clojure-webapp.ui.boundary.http-helper
  (:require [clj-http.client :as client]))

(def ^:private base-url "http://localhost:3000")

(defn http-get [path]
  (client/get (str base-url path)
              {:accept :json
               :as :json
               :coerce :always
               :throw-exceptions? false}))

(defn http-post [path body]
  (client/post (str base-url path)
               {:form-params body
                :content-type :json
                :accept :json
                :as :json
                :coerce :always
                :throw-exceptions? false}))

(defn http-put [path body]
  (client/put (str base-url path)
              {:form-params body
               :content-type :json
               :accept :json
               :as :json
               :coerce :always
               :throw-exceptions? false}))

(defn http-delete [path]
  (client/delete (str base-url path)
                 {:accept :json
                  :as :json
                  :coerce :always
                  :throw-exceptions? false}))
