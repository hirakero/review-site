(ns isomorphic-clojure-webapp.ui.handler.reviews
  (:require
   [integrant.core :as ig]
   [isomorphic-clojure-webapp.ui.response :refer [ok]]
   [isomorphic-clojure-webapp.ui.boundary.products :as products]
   [ring.util.http-response :as res]))

(defmethod ig/init-key ::create [_ _]
  (fn [{:keys [path-params]}]
    (ok [:div.is-size-4 "review create"]
        [:form.box {:method :post :action ""}
         [:div.field
          [:label.label {:for "title"} "title"]
          [:div.control
           [:input.input {:type :text :name "title" :id "title" :required "required"}]]]
         [:div.field
          [:label.label {:for "content"} "content"]
          [:div.control
           [:input.input {:type :text :name "content" :id "content"}]]]
         [:div.field
          [:label.label {:for "rate"} "rate"]
          [:div.control
           [:input.input {:type :number :name "rate" :id "rate"}]]]

         [:button.button {:type :submit} "create"]])))

(defmethod ig/init-key ::create-post [_ {:keys [host]}]
  (fn [{:keys [form-params path-params]}]
    (let [{:strs [name description]} form-params
          {:keys [status]} (products/create-product host form-params)]
      (if (= status 201)
        (res/see-other (str "/products/" (:product-id path-params) "/detail"))
        (ok [:h2  "product create"]
            [:div "error"])))))
