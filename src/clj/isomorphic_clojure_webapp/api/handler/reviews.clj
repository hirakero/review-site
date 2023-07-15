(ns isomorphic-clojure-webapp.api.handler.reviews
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.reviews :as reviews]
            [isomorphic-clojure-webapp.api.boundary.products :as products]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [ring.util.response :as rres]))

(defmethod ig/init-key ::list [_ {:keys [db]}]
  (fn [req]
    {:body {:reviews {}}}))
(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [req]
    {:body {:review {}}}))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params path-params identity]}]
    (let [{:keys [product-id]} path-params
          user-id (:sub identity)
          product (products/get-product-by db :id product-id)
          user (users/get-user-by db :id user-id)]
      (if-not product
        (rres/not-found {:error "product not found"})
        (if-not user
          (rres/not-found {:error "user not found"})
          (let [params (assoc body-params :product-id product-id :user-id user-id)]
            (if-let [result (reviews/create-review db params)]
              (rres/created (str "/api/reviews/" (:id result)) result)
              (rres/bad-request {:error ""}))))))))

#_(defmethod ig/init-key ::update [_ {:keys [db]}]
    (fn [req]))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params identity]}]
    (let [review-id (:review-id path-params)
          target (reviews/get-review-by-id db review-id)
          user-id (:sub identity)]
      ;TODO 対象データがなくても成功にする？
      (if (nil? target)
        (rres/not-found {:error "review not found"})
        (if (= (-> target :user-id str) user-id)
          (if-let [result (reviews/delete-review db review-id)]
            {:status 204}
            (rres/not-found {:error "review not found"}))
          {:status 403
           :body {:error "permission error"}})))))
