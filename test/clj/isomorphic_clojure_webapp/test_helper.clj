(ns isomorphic-clojure-webapp.test-helper
  (:require [clj-http.client :as client]))

(def ^:private test-url "http://localhost:3000")

(defn http-get [path]
  (client/get (str test-url path)
              {:accept :json
               :as :json
               :coerce :always
               :throw-exceptions? false}))



(defn http-post [path body]
  (client/post (str test-url path)
               {:form-params body
                :content-type :json
                :accept :json
                :as :json
                :coerce :always
                :throw-exceptions? false})  )

(http-post "/users"  {:name "Charly"})

(defn http-put [path body]
  (client/put (str test-url path)
              {:form-params body
               :content-type :json
               :accept :json
               :as :json
               :coerce :always
               :throw-exceptions? false}))

(defn http-delete [path]
  (client/delete (str test-url path)
                 {:accept :json
                  :as :json
                  :coerce :always
                  :throw-exceptions? false}))
