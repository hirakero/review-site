(ns isomorphic-clojure-webapp.ui.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.ui.response :refer [ok]]
            [isomorphic-clojure-webapp.ui.boundary.users :as users]
            [isomorphic-clojure-webapp.ui.view.template :refer [template]]
            [ring.util.http-response :as res]))

;; (defmethod ig/init-key ::list [_ {:keys [host]}]
;;     (fn [{qp :query-params}]
;;       (let [users  (-> (users/get-users host qp) :body :users)]
;;         (apply ok (template  [:a {:href "/users/signup"} "create"]
;;                              [:ul (for [{:keys [name id]} users]
;;                                     [:li [:a {:href (str "/users/detail/" id)} name]])])))))

(defn- strkey->kwkey [m]
  (reduce-kv (fn [m k v] (assoc m (keyword k) v)) {} m)) ;TODO middlewareで

(defmethod ig/init-key ::detail [_ {:keys [host]}]
  (fn [{{:keys [user-id]} :path-params}]
    (let [{:keys [name email password]} (-> (users/get-user-by-id host user-id) :body :user)]
      (ok [:h2 "user detail"]
          [:dl
           [:dt  "name:"] [:dd name]
           [:dt  "email: "] [:dd email]
           [:dt  "passrord: "] [:dd "*****"]]
          [:div
           [:a {:href (str "/users/edit/" user-id)} "edit"] " / " [:a {:href (str "/users/delete/" user-id)} "delete"]]
          [:hr]
          (for [_ (range 5)]
            [:div [:h3 "product name"]
             [:div "date"]
             [:div "title"]
             [:div "content"]])))))

(defmethod ig/init-key ::create [_ _]
  (fn [req]
    (ok [:div.is-size-4 "user create"]
        [:form.box {:method :post :action ""}
         [:div.field
          [:label.label {:for "name"} "name"]
          [:input.input {:type :text :name "name" :id "name" :required "required"}]]
         [:div.field
          [:label.label {:for "email"} "email"]
          [:div.control
           [:input.input {:type :text :name "email" :id "email"}]]]
         [:div.field
          [:label.label {:for "password"} "password"]
          [:div.control
           [:input.input {:type :text :name "password" :id "password"}]]]
         [:button.button {:type :submit} "create"]])))

(defmethod ig/init-key ::create-post [_ {:keys [host]}]
  (fn [{:keys [form-params]}]
    (let [form-params (strkey->kwkey form-params)
          {:keys [status]} (users/create-user host form-params)]
      (if (= status 201)
        (res/see-other (str "/"))
        (ok [:h2  "user create"]
            [:div "error"])))))

(defmethod ig/init-key ::edit [_ {:keys [host]}]
  (fn [{:keys [path-params]}]
    (if-let [user (-> (users/get-user-by-id host (:user-id path-params)) :body :user)]
      (ok [:div.is-size-4 "user edit"]
          [:form.box {:method :post :action ""}
           [:dl
            [:div.field
             [:label.label {:for "name"} "name"]
             [:div.control
              [:input.input {:type :text :name "name" :id "name" :required "required" :value (:name user)}]]]
            [:div.field
             [:label.label {:for "email"} "email"]
             [:div.control
              [:input.input {:type :text :name "email" :id "email" :required "required" :value (:email user)}]]]
            [:div.field
             [:label.label {:for "password"} "password"]
             [:input.input {:type :text :name "passowrd" :id "password" :required "required"}]]]
           [:button.button {:type :submit} "update"]])
      (ok [:h2 "user edit"]
          [:div "not found"]))))

(defmethod ig/init-key ::edit-post [_ {:keys [host]}]
  (fn [{:keys [form-params path-params]}]
    (let [form-params (strkey->kwkey form-params)
          {:keys [status body]}  (users/update-user host (:user-id path-params) form-params)]
      (if (= status 200)
        (res/see-other (str "/"))
        (ok [:h2  "user edit"]
            [:div "error"])))))

(defmethod ig/init-key ::delete [_ {:keys [host]}]
  (fn [{:keys [path-params] :as request}]
    (let [{:keys [status body]}  (users/get-user-by-id host (:user-id path-params))]
      (if (= status 200)
        (ok [:h2 "user delete"]
            [:form {:method :post :action ""}
             [:dl
              [:dt "name:"] [:dd (-> body :user :name)]]
             [:div
              [:button {:type :submit} "delete"]]])
        (ok [:h2  "user delete"]
            [:div "not found"])))))

(defmethod ig/init-key ::delete-post [_ {:keys [host]}]
  (fn [{:keys [path-params]}]
    (let [{:keys [status]} (users/delete-user host (:user-id path-params))]
      (if (= 204 status)
        (res/see-other (str "/"))
        (ok [:h2 "user delete post"]
            [:div "error"])))))

(defmethod ig/init-key ::signin [_ _]
  (fn [req]
    (ok [:div.is-size-4 "sign in"]
        [:form.box {:method :post :action ""}
         [:div.field
          [:label.label {:for "name"} "user name or email"]
          [:div.control
           [:input.input {:type :text :name "name" :id "name" :required "required"}]]]
         [:div.field
          [:label.label {:for "password"} "password"]
          [:div.control
           [:input.input {:type :password :name "password" :id "password"}]]]
         [:button.button {:type :submit} "sign in"]]
        [:a {:href "/users/signup"} "sign up here"])))

(defmethod ig/init-key ::signin-post [_ {:keys [host]}]
  (fn [{:keys [form-params]}]
    (let [form-params (strkey->kwkey form-params)
          {:keys [status] :as result} (users/signin host form-params)
          _ (println form-params " | " result)]
      (if (= status 200)
        (res/see-other (str "/")) ;TODO flash message
        (ok [:h2  "user signin"]
            [:div "error"])))))
