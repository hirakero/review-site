(ns isomorphic-clojure-webapp.ui.handler.users
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.ui.response :refer [ok see-other]]
            [isomorphic-clojure-webapp.ui.boundary.users :as users]
            [isomorphic-clojure-webapp.ui.view.template :refer [template]]))

;; (defmethod ig/init-key ::list [_ {:keys [host]}]
;;     (fn [{qp :query-params}]
;;       (let [users  (-> (users/get-users host qp) :body :users)]
;;         (apply ok (template  [:a {:href "/users/signup"} "create"]
;;                              [:ul (for [{:keys [name id]} users]
;;                                     [:li [:a {:href (str "/users/detail/" id)} name]])])))))

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
    (ok [:h2 "user create"]
        [:form {:method :post :action ""}
         [:dl
          [:dt [:label {:for "name"} "name"]]
          [:dd [:input {:type :text :name "name" :id "name" :required "required"}]]
          [:dt [:label {:for "email"} "email"]]
          [:dd [:input {:type :text :name "email" :id "email"}]]
          [:dt [:label {:for "password"} "password"]]
          [:dd [:input {:type :text :name "password" :id "password"}]]]
         [:div
          [:button {:type :submit} "create"]]])))

(defmethod ig/init-key ::create-post [_ {:keys [host]}]
  (fn [{:keys [form-params]}]
    (let [{:strs [name email password]} form-params
          form-params ()]
      (let [{:keys [status]} (users/create-user host form-params)]
        (if (= status 201)
          (see-other (str "/"))
          (ok [:h2  "user create"]
              [:div "error"]))))))

(defmethod ig/init-key ::edit [_ {:keys [host]}]
  (fn [{:keys [path-params]}]
    (if-let [user (-> (users/get-user-by-id host (:user-id path-params)) :body :user)]
      (ok [:h2 "user edit"]
          [:form {:method :post :action ""}
           [:dl
            [:dt [:label {:for "name"} "name"]]
            [:dd [:input {:type :text :name "name" :id "name" :required "required" :value (:name user)}]]
            [:dt [:label {:for "email"} "email"]]
            [:dd [:input {:type :text :name "email" :id "email" :required "required" :value (:email user)}]]
            [:dt [:label {:for "password"} "password"]]
            [:dd [:input {:type :text :name "passowrd" :id "password" :required "required"}]]]
           [:div
            [:button {:type :submit} "update"]]])
      (ok [:h2 "user edit"]
          [:div "not found"]))))

(defmethod ig/init-key ::edit-post [_ {:keys [host]}]
  (fn [{:keys [form-params path-params]}]
    (let [{:keys [status body]}  (users/update-user host (:user-id path-params) form-params)]
      (if (= status 200)
        (see-other (str "/"))
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
        (see-other (str "/"))
        (ok [:h2 "user delete post"]
            [:div "error"])))))

(defmethod ig/init-key ::signin [_ _]
  (fn [req]
    (ok [:h2 "sign in"]
        [:form {:method :post :action ""}
         [:dl
          [:dt [:label {:for "name"} "user name or password"]]
          [:dd [:input {:type :text :name "name" :id "name" :required "required"}]]
          [:dt [:label {:for "password"} "password"]]
          [:dd [:input {:type :text :name "password" :id "password"}]]]
         [:div
          [:button {:type :submit} "sign in"]]]
        [:a {:href "/users/signup"} "sign up here"])))

(defmethod ig/init-key ::signin-post [_ {:keys [host]}]
  (fn [{:keys [form-params]}]
    (let [{:strs [name email password]} form-params]
      (let [{:keys [status]} (users/signin host form-params)]
        (if (= status 201)
          (see-other (str "/"))
          (ok [:h2  "user signin"]
              [:div "error"]))))))