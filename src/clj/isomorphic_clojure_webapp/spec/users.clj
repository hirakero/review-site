(ns isomorphic-clojure-webapp.spec.users
  (:require [clojure.spec.alpha :as s]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(s/def ::name string?)
(s/def ::password (s/and string?
                         #(>= (count %) 8)))

(s/def ::email (s/and string? #(re-matches email-regex %)))

(s/def ::signin-body (s/keys :req-un [(or ::name ::email) ::password]))
(s/def ::signup-body (s/keys :req-un [::name ::email ::password]))
(s/def ::put-body (s/keys :req-un [(or ::name ::email ::password)]))
