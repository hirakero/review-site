(ns isomorphic-clojure-webapp.ui.handler.products
  (:require [integrant.core :as ig]
            [rum.core :as rum]
            [isomorphic-clojure-webapp.ui.boundary.products :as products]))

(defn ok [& body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-html [:html
                           [:head
                            [:title "review site"]
                            [:link {:rel "stylesheet"
                                    :href "https://cdn.jsdelivr.net/gh/kognise/water.css@latest/dist/light.min.css"}]]
                           [:body body]])})

(defn see-other [location]
  {:status 303
   :headers {"content-type" "text/html"
             "Location" location}})

(defmethod ig/init-key ::list [_ _]
  (fn [{qp :query-params}]
    (let [products  (products/get-products qp)]
      (ok [:h2 "product list"]
          [:a {:href "/ui/products/create"} "create"]
          [:ul (for [{:keys [name id]} products]
                 [:li [:a {:href (str "/ui/products/detail/" id)} name]])]))))

(defmethod ig/init-key ::detail [_ _]
  (fn [{{:keys [product-id]} :path-params}]
    (let [{:keys [name description]} (products/get-product-by-id product-id)]
      (ok [:h2 "product detail"]
          [:dl
           [:dt  "name:"] [:dd name]
           [:dt  "description: "] [:dd description]]
          [:div
           [:a {:href (str "/ui/products/edit/" product-id)} "edit"] " / " [:a {:href (str "/ui/products/delete/" product-id)} "delete"]]
          [:div
           [:a {:href "/ui/products"} "product list"]]))))

(defmethod ig/init-key ::create [_ _]
  (fn [req]
    (ok [:h2 "product create"]
        [:form {:method :post :action ""}
         [:dl
          [:dt [:label {:for "name"} "name"]]
          [:dd [:input {:type :text :name "name" :id "name" :required "required"}]]
          [:dt [:label {:for "description"} "description"]]
          [:dd [:input {:type :text :name "description" :id "description"}]]]
         [:div
          [:button {:type :submit} "create"]]])))

(defmethod ig/init-key ::create-post [_ _]
  (fn [{:keys [form-params]}]
    (let [{:strs [name description]} form-params]
      (let [{:keys [status]} (products/create-product form-params)]
        (if (= status 201)
          (see-other (str "/ui/products"))
          (ok [:h2  "product create"]
              [:div "error"]))))))

(defmethod ig/init-key ::edit [_ _]
  (fn [{:keys [path-params]}]
    (if-let [result (products/get-product-by-id (:product-id path-params))]
      (ok [:h2 "product edit"]
          [:form {:method :post :action ""}
           [:dl
            [:dt [:label {:for "name"} "name"]]
            [:dd [:input {:type :text :name "name" :id "name" :required "required" :value (:name result)}]]
            [:dt [:label {:for "description"} "description"]]
            [:dd [:input {:type :text :name "description" :id "description" :value (:description result)}]]]
           [:div
            [:button {:type :submit} "update"]]])
      (ok [:h2 "product edit"]
          [:div "not found"]))))

(defmethod ig/init-key ::edit-post [_ _]
  (fn [{:keys [form-params path-params]}]
    (let [{:keys [status body]}  (products/update-product (:product-id path-params) form-params)]
      (if (= status 200)
        (see-other (str "/ui/products"))
        (ok [:h2  "product edit"]
            [:div "error"])))))

(defmethod ig/init-key ::delete [_ _]
  (fn [{:keys [path-params]}]
    (let [{:keys [status body]} (products/get-product-by-id (:product-id path-params))]
      (if (= status 200)
        (ok [:h2 "product delete"]
            [:form {:method :post :action ""}
             [:dl
              [:dt "name:"] [:dd (:name body)]
              [:dt "description:"] [:dd (:description body)]]
             [:div
              [:button {:type :submit} "delete"]]])
        (ok [:h2  "product delete"]
            [:div "not found"])))))

(defmethod ig/init-key ::delete-post [_ _]
  (fn [{:keys [path-params]}]
    (let [{:keys [status]} (products/delete-product (:product-id path-params))]
      (if (= 204 status)
        (see-other (str "/ui/products"))
        (ok [:h2 "product delete post"]
            [:div "error"])))))
