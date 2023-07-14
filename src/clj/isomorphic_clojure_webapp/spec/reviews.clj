(ns isomorphic-clojure-webapp.spec.reviews
  (:require [clojure.spec.alpha :as s]))

(def uuid-regex #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
(s/def :isomorphic-clojure-webapp.spec/uuid (s/and string? #(re-matches uuid-regex %)))


(s/def ::review-id (s/and string? #(re-matches uuid-regex %)))

#_(s/def ::product-id :isomorphic-clojure-webapp.spec/uuid)
#_(s/def ::user-id :isomorphic-clojure-webapp.spec/uuid)
(s/def ::title string?)
(s/def ::content string?)
(s/def ::rate (s/and int? #(<= 1 % 5)))

(s/def ::post-body (s/keys :req-un [::title ::content ::rate]))
(s/def ::reviews-path (s/keys :req-un [::review-id]))
#_(s/def ::product-reviews-path (s/keys :req-un [::product-id]))
#_(s/def ::user-reviews-path (s/keys :req-un [::product-id]))