(ns isomorphic-clojure-webapp.api.handler.reviews
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.reviews :as reviews]
            [isomorphic-clojure-webapp.api.boundary.products :as products]
            [isomorphic-clojure-webapp.api.boundary.users :as users]
            [ring.util.http-response :as res]))

(defmethod ig/init-key ::list [_ {:keys [db]}]
  (fn [_]
    (let [reviews (reviews/get-reviews db {})]
      (if (empty? reviews)
        (res/not-found! {:error "reviews not found"})
        (res/ok {:reviews reviews})))))

(defmethod ig/init-key ::list-by-product [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [product-id (:product-id path-params)
          product (products/get-product-by db :id product-id)]
      (if-not product
        (res/not-found! {:error "product not found"})
        (let [reviews (reviews/get-reviews-by-product db product-id)]
          (if (empty? reviews)
            (res/not-found! {:error "reviews not found"})
            (res/ok {:reviews reviews})))))))


(defmethod ig/init-key ::list-by-user [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [user-id (:user-id path-params)
          user (users/get-user-by db :id user-id)]
      (if-not user
        (res/not-found! {:error "user not found"})
        (let [reviews (reviews/get-reviews-by-user db user-id)]
          (if (empty? reviews)
            (res/not-found! {:error "reviews not found"})
            (res/ok {:reviews reviews})))))))


(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [{:keys [path-params]}]
    (let [review-id (:review-id path-params)
          review (reviews/get-review-by-id db review-id)]
      (if (empty? review)
        (res/not-found! {:error "review not found"})
        (res/ok {:review review})))))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{:keys [body-params path-params identity]}]
    (let [{:keys [product-id]} path-params
          user-id (:sub identity)
          product (products/get-product-by db :id product-id)
          user (users/get-user-by db :id user-id)]
      (if-not product
        (res/not-found! {:error "product not found"})
        (if-not user
          (res/not-found! {:error "user not found"})
          (let [params (assoc body-params :product-id product-id :user-id user-id)]
            (if-let [result (reviews/create-review db params)]
              (res/created (str "/api/reviews/" (:id result)) result)
              (res/internal-server-error! {:error ""}))))))))

#_(defmethod ig/init-key ::update [_ {:keys [db]}]
    (fn [req]))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params identity]}]
    (let [review-id (:review-id path-params)
          target (reviews/get-review-by-id db review-id)
          user-id (:sub identity)]
      (if-not target
        (res/not-found! {:error "review not found"})
        (if (= (-> target :user-id str) user-id)
          (if-let [result (reviews/delete-review db review-id)]
            (res/no-content)
            (res/not-found! {:error "review not found"}))
          (res/forbidden! {:error "owner only"}))))))
