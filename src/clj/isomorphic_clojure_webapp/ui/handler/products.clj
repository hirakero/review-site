(ns isomorphic-clojure-webapp.ui.handler.products
  (:require [integrant.core :as ig]
            [rum.core :as rum]
            [clj-http.client :as client]))

(defn common [& body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-html [:html
                           [:body body]])})

(def ^:private base-url "http://localhost:3000")

(defn http-get [path]
  (client/get (str base-url path)
              {:accept :json
               :as :json
               :coerce :always
               :throw-exceptions? false}))

(defn http-post [path body]
  (client/post (str base-url path)
               {:form-params body
                :content-type :json
                :accept :json
                :as :json
                :coerce :always
                :throw-exceptions? false}))

(comment
  (-> (http-get "/api/products")
      :body
      :products))

(defmethod ig/init-key ::list [_ _]
  (fn [req]
    (let [products (-> (http-get "/api/products")
                       :body
                       :products)]
      (common [:h2 "product list"]
              [:ul (for [{:keys [name id]} products]
                     [:li [:a {:href (str "/ui/products/detail/" id)} name]])]))))

(defmethod ig/init-key ::detail [_ _]
  (fn [{:keys [path-params]}]
    (let [{:keys [name description]} (-> (http-get (str "/api/products/" (:product-id path-params)))
                                         :body
                                         :product)]
      (common [:h2 "product detail"]
              [:p "name: " name]
              [:p "description: " description]
              [:p [:a {:href "/ui/products"} "product list"]]))))

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
              [:button {:type :submit} "send"]]])))

(defmethod ig/init-key ::create-post [_ _]
  (fn [{:keys [form-params] :as all}]
    (let [{:strs [name description]} form-params]
      (if-let [result (-> (http-post "/api/products" {:name name
                                                      :description description})
                          :body
                          :product)]
        (common [:h2  "product created"]
                [:div (str "name: " name)]
                [:div (str "description: " description)]) ;redirect
        (common [:h2  "product create"]
                [:div "error"])))))

(defmethod ig/init-key ::edit [_ _]
  (fn [req]
    (common [:h2 "product edit"])))

(defmethod ig/init-key ::edit-post [_ _]
  (fn [req]
    (common [:h2 "product edit post"])))

(defmethod ig/init-key ::delete [_ _]
  (fn [req]
    (common [:h2 "product delete"])))

(defmethod ig/init-key ::delete-post [_ _]
  (fn [req]
    (common [:h2 "product delete post"])))


