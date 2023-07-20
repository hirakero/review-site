(ns isomorphic-clojure-webapp.spec.products
  (:require [clojure.spec.alpha :as s]))
(def uuid-regex #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

(s/def ::product-id (s/and string? #(re-matches uuid-regex %)))
(s/def ::name string?)
(s/def ::description (s/or :s string? :n nil?))
(s/def ::limit pos-int?)
(s/def ::offset pos-int?)
(s/def ::sort string?)
(s/def ::order #(-> % .toUpperCase  #{"ASC" "DESC"}))

(s/def ::product-path (s/keys :req-un [::product-id]))

(s/def ::query (s/keys :opt-un [::name ::description ::limit ::offset ::sort ::order]))

(s/def ::post-body (s/keys :req-un [::name ::description]))
(s/def ::put-body (s/keys :req-un [(or ::name ::description)]))
