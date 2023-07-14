(ns isomorphic-clojure-webapp.api.handler.reviews
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.api.boundary.reviews :as reviews]
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
          user-id (:sub identity)]
      (let [params (assoc body-params :product-id product-id :user-id user-id)]
        (if-let [result (reviews/create-review db params)]
          (rres/created (str "/api/reviews/" (:id result)) result)
          (rres/bad-request {:error ""}))))))

#_(defmethod ig/init-key ::update [_ {:keys [db]}]
    (fn [req]))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [{:keys [path-params identity]}]
    (let [review-id (:review-id path-params)
          target (reviews/get-review-by-id db review-id)
          user-id (:sub identity)
          _ (println "\ntarget " target)
          _ (println "\npp " path-params)]
      (if (nil? target)
        (rres/not-found {:error "not-found"})
        (if (= (-> target :user-id str) user-id)
          (if-let [result (reviews/delete-review db review-id)]
            (do
              (println "\nresult " result)
              {:status 204})
            (do
              (println "\nnot-found ")
              (rres/not-found {:error "not-found"})))
          {:status 403
           :body {}})))))
