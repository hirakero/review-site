(ns isomorphic-clojure-webapp.api.exception-handler
  (:require [integrant.core :as ig]
            [reitit.ring.middleware.exception :as exception]))

(defmethod ig/init-key ::wrap-exception-handler [_ _]
  exception/exception-middleware)



