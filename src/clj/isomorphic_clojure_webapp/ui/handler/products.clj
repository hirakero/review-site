(ns isomorphic-clojure-webapp.ui.handler.products
  (:require [integrant.core :as ig]
            [rum.core :as rum]
            [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]))

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
    (let [query (->> (keys qp) (map #(str % "=" (get qp %))) (clojure.string/join "&"))
          products (-> (helper/http-get (str "/api/products" (when-not (empty? query) (str "?" query))))
                       :body
                       :products)]
      (ok [:h2 "product list"]
          [:a {:href "/ui/products/create"} "create"]
          [:ul (for [{:keys [name id]} products]
                 [:li [:a {:href (str "/ui/products/detail/" id)} name]])]))))

(defmethod ig/init-key ::detail [_ _]
  (fn [{{:keys [product-id]} :path-params}]
    (let [{:keys [name description]} (-> (helper/http-get (str "/api/products/" product-id))
                                         :body
                                         :product)]
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
      (let [{:keys [status]}  (helper/http-post "/api/products" {:name name
                                                                 :description description})]
        (if (= status 201)
          (see-other (str "/ui/products"))
          (ok [:h2  "product create"]
              [:div "error"]))))))

(defmethod ig/init-key ::edit [_ _]
  (fn [{:keys [path-params]}]
    (if-let [result (-> (helper/http-get (str "/api/products/" (:product-id path-params)))
                        :body
                        :product)]
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
    (let [{:strs [name description]} form-params]
      (let [{:keys [status body]}  (helper/http-put (str "/api/products/" (:product-id path-params)) {:name name
                                                                                                      :description description})]
        (if (= status 200)
          (see-other (str "/ui/products"))
          (ok [:h2  "product edit"]
              [:div "error"]))))))

(defmethod ig/init-key ::delete [_ _]
  (fn [{:keys [path-params]}]
    (if-let [result (-> (helper/http-get (str "/api/products/" (:product-id path-params)))
                        :body
                        :product)]
      (ok [:h2 "product delete"]
          [:form {:method :post :action ""}
           [:dl
            [:dt "name:"] [:dd (:name result)]
            [:dt "description:"] [:dd (:description result)]]
           [:div
            [:button {:type :submit} "delete"]]])
      (ok [:h2  "product delete"]
          [:div "not found"]))))

(defmethod ig/init-key ::delete-post [_ _]
  (fn [{:keys [path-params]}]
    (let [{:keys [status]} (-> (helper/http-delete (str "/api/products/" (:product-id path-params))))]
      (if (= 204 status)
        (see-other (str "/ui/products"))
        (ok [:h2 "product delete post"]
            [:div "error"])))))