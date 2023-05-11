(ns isomorphic-clojure-webapp.ui.handler.products
  (:require [integrant.core :as ig]
            [rum.core :as rum]
            [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]))

(defn common [& body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-html [:html
                           [:body body]])})

(defmethod ig/init-key ::list [_ _]
  (fn [req]
    (let [products (-> (helper/http-get "/api/products")
                       :body
                       :products)]
      (common [:h2 "product list"]
              [:ul (for [{:keys [name id]} products]
                     [:li [:a {:href (str "/ui/products/detail/" id)} name]])]
              [:a {:href "/ui/products/create"} "create"]))))

(defmethod ig/init-key ::detail [_ _]
  (fn [{{:keys [:product-id]} :path-params}]
    (let [{:keys [name description]} (-> (helper/http-get (str "/api/products/" product-id))
                                         :body
                                         :product)]
      (common [:h2 "product detail"]
              [:div (str "name: " name)]
              [:div (str "description: " description)]

              [:div [:a {:href (str "/ui/products/edit/" product-id)} "edit"] " / " [:a {:href (str "/ui/products/delete/" product-id)} "delete"]]
              [:div [:a {:href "/ui/products"} "product list"]]))))

(defmethod ig/init-key ::create [_ _]
  (fn [req]
    (common [:h2 "product create"]
            [:form {:method :post :action ""}
             [:div
              [:label {:for "name"} "name"]
              [:input {:type :text :name "name" :id "name"}]]
             [:div
              [:label {:for "description"} "description"]
              [:input {:type :text :name "description" :id "description"}]]
             [:div
              [:button {:type :submit} "create"]]])))

(defmethod ig/init-key ::create-post [_ _]
  (fn [{:keys [form-params]}]
    (let [{:strs [name description]} form-params]
      (if-let [result (-> (helper/http-post "/api/products" {:name name
                                                             :description description})
                          :body
                          :product)]
        (common [:h2  "product created"]
                [:div (str "name: " name)]
                [:div (str "description: " description)]) ;redirect 303 (See Other)
        (common [:h2  "product create"]
                [:div "error"])))))

(defmethod ig/init-key ::edit [_ _]
  (fn [{:keys [path-params]}]
    (println "pp " path-params)
    (if-let [result (-> (helper/http-get (str "/api/products/" (:product-id path-params)))
                        :body
                        :product)]
      (common [:h2 "product edit"]
              [:form {:method :post :action ""}
               [:div
                [:label {:for "name"} "name"]
                [:input {:type :text :name "name" :id "name" :value (:name result)}]]
               [:div
                [:label {:for "description"} "description"]
                [:input {:type :text :name "description" :id "description" :value (:description result)}]]
               [:div
                [:button {:type :submit} "update"]]])
      (common [:h2 "product edit"]
              [:div "not found"]))))

(defmethod ig/init-key ::edit-post [_ _]
  (fn [{:keys [form-params path-params]}]
    (let [{:strs [name description]} form-params]
      (if-let [result (-> (helper/http-put (str "/api/products/" (:product-id path-params)) {:name name
                                                                                             :description description})
                          :body
                          :product)]
        (common [:h2  "product edit"]
                [:div (str "name: " name)]
                [:div (str "description: " description)]) ;redirect 303 (See Other)
        (common [:h2  "product edit"]
                [:div "error"])))))

(defmethod ig/init-key ::delete [_ _]
  (fn [{:keys [path-params]}]
    (if-let [result (-> (helper/http-get (str "/api/products/" (:product-id path-params)))
                        :body
                        :product)]
      (common [:h2 "product delete"]
              [:form {:method :post :action ""}
               [:div
                [:p "name: " (:name result)]]
               [:div
                [:p "description: " (:description result)]]
               [:div
                [:button {:type :submit} "delete"]]])
      (common [:h2  "product delete"]
              [:div "not found"]))))

(defmethod ig/init-key ::delete-post [_ _]
  (fn [{:keys [path-params]}]
    (let [{:keys [status body]} (-> (helper/http-delete (str "/api/products/" (:product-id path-params))))]
      (if (= 204 status)
        (common [:h2 "product delete"]
                [:div "success"])
        (common [:h2 "product delete post"]
                [:div "error"])))))

