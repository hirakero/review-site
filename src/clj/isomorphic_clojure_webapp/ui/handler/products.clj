(ns isomorphic-clojure-webapp.ui.handler.products
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.ui.response :refer [ok]]
            [isomorphic-clojure-webapp.ui.boundary.products :as products]
            [ring.util.http-response :as res]))


(defmethod ig/init-key ::list [_ {:keys [host]}]
  (fn [{qp :query-params}]
    (let [{:keys [status body]}  (products/get-products host qp)]
      (ok [:h2 "product list"]
          [:a {:href "/products/create"} "create"]
          [:ul (for [{:keys [name id]} (:products body)]
                 [:li [:a {:href (str "/products/detail/" id)} name]])]))))

(defmethod ig/init-key ::search [_ {:keys [host]}]
  (fn [{{:keys [product-id]} :path-params
        qp :query-params}]
    (let [products (products/get-products host qp)]


      (ok [:h2 "商品検索"]
          [:dl
           [:label  "name:"] [:input {:type :text}]
           [:button "検索"]]
          #_[:div
             [:a {:href (str "/products/edit/" product-id)} "edit"] " / " [:a {:href (str "/products/delete/" product-id)} "delete"]]))))

(defmethod ig/init-key ::detail [_ {:keys [host]}]
  (fn [{{:keys [product-id]} :path-params}]
    (let [{:keys [status body]} (products/get-product-by-id host product-id)
          {:keys [name description]} (:product body)]

      (ok [:h2 "商品情報"]
          [:div name]
          [:dl
           [:dt  "name:"] [:dd name]
           [:dt  "description: "] [:dd description]]
          [:div "レビューを書く"]
          [:div  "レビュ一覧"]
          #_[:div
             [:a {:href (str "/products/edit/" product-id)} "edit"] " / " [:a {:href (str "/products/delete/" product-id)} "delete"]]
          [:hr]
          (for [_ (range 5)]
            [:div [:h3 "title"]
             [:div "content"]
             [:div "date"]
             [:div "user name"]])))))

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

(defmethod ig/init-key ::create-post [_ {:keys [host]}]
  (fn [{:keys [form-params]}]
    (let [{:strs [name description]} form-params]
      (let [{:keys [status]} (products/create-product host form-params)]
        (if (= status 201)
          (res/see-other (str "/products"))
          (ok [:h2  "product create"]
              [:div "error"]))))))

(defmethod ig/init-key ::edit [_ {:keys [host]}]
  (fn [{:keys [path-params]}]
    (if-let [{{:keys [product]} :body} (products/get-product-by-id  host (:product-id path-params))]
      (ok [:h2 "product edit"]
          [:form {:method :post :action ""}
           [:dl
            [:dt [:label {:for "name"} "name"]]
            [:dd [:input {:type :text :name "name" :id "name" :required "required" :value (:name product)}]]
            [:dt [:label {:for "description"} "description"]]
            [:dd [:input {:type :text :name "description" :id "description" :value (:description product)}]]]
           [:div
            [:button {:type :submit} "update"]]])
      (ok [:h2 "product edit"]
          [:div "not found"]))))

(defmethod ig/init-key ::edit-post [_ {:keys [host]}]
  (fn [{:keys [form-params path-params]}]
    (let [{:keys [status body]}  (products/update-product host (:product-id path-params) form-params)]
      (if (= status 200)
        (res/see-other (str "/products"))
        (ok [:h2  "product edit"]
            [:div "error"])))))

(defmethod ig/init-key ::delete [_ {:keys [host]}]
  (fn [{:keys [path-params]}]
    (let [{:keys [status body]} (products/get-product-by-id host (:product-id path-params))
          {:keys [name description]} (:product body)]
      (if (= status 200)
        (ok [:h2 "product delete"]
            [:form {:method :post :action ""}
             [:dl
              [:dt "name:"] [:dd name]
              [:dt "description:"] [:dd description]]
             [:div
              [:button {:type :submit} "delete"]]])
        (ok [:h2  "product delete"]
            [:div "not found"])))))

(defmethod ig/init-key ::delete-post [_ {:keys [host]}]
  (fn [{:keys [path-params]}]
    (let [{:keys [status]} (products/delete-product host (:product-id path-params))]
      (if (= 204 status)
        (res/see-other (str "/products"))
        (ok [:h2 "product delete post"]
            [:div "error"])))))


#_(defn get-new-products [host item-count]
    (let [products (products/get-products host {:limit 5})]))
