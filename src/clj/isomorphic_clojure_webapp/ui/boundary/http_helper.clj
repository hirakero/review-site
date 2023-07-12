(ns isomorphic-clojure-webapp.ui.boundary.http-helper
  (:require [clj-http.client :as client]))

(def ^:private base-url "http://localhost:3000")

(defn http-get
  ([path] (http-get path {}))
  ([path headers]
   (client/get (str base-url path)
               {:headers headers
                :accept :json
                :as :json
                :coerce :always
                :throw-exceptions? false})))

(defn http-post
  ([path body] (http-post path {} body))
  ([path headers body]
   (client/post (str base-url path)
                {:headers headers
                 :form-params body
                 :content-type :json
                 :accept :json
                 :as :json
                 :coerce :always
                 :throw-exceptions? false})))
(defn http-put
  ([path body] (http-put path {} body))
  ([path headers body]
   (client/put (str base-url path)
               {:headers headers
                :form-params body
                :content-type :json
                :accept :json
                :as :json
                :coerce :always
                :throw-exceptions? false})))

(defn http-delete
  ([path] (http-delete path {}))
  ([path headers]
   (client/delete (str base-url path)
                  {:headers headers
                   :accept :json
                   :as :json
                   :coerce :always
                   :throw-exceptions? false})))
