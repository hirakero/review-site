(ns isomorphic-clojure-webapp.spec.products
  (:require [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::description (s/or :s string? :n nil?))
(s/def ::limit pos-int?)
(s/def ::offset pos-int?)
(s/def ::sort string?)
(s/def ::order #(-> % .toUpperCase  #{"ASC" "DESC"}))
#_(s/valid? ::order "DESC")

(s/def ::query-params (s/keys :opt-un [::name ::description ::limit ::offset ::sort ::order]))
