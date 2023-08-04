(ns isomorphic-clojure-webapp.ui.view.template)

(defn template [& body]
  [[:header
    [:div.navbar
     [:div.navbar-brand
      [:div.navbar-item.is-size-3 [:a {:href "/"} "review site"]]]
     [:div.navbar-menu
      [:div.navbar-start
       [:div.navbar-item [:a {:href "/"} "home"]]
       [:div.navbar-item [:a {:href "/about"} "about"]]
       [:div.navbar-item [:a {:href "/contact"} "contact"]]]
      [:div.navbar-end
       [:div.navbar-item
        [:form {:method :get :action ""}
         [:div.field.has-addons
          [:div.control
           [:input.input {:type :text :name "search" :placeholder "product name etc"}]]
          [:div.control
           [:a.button.is-info [:i.fa.fa-search] "search"]
           #_[:button.column.is-2 {:type :submit} "search"]]]]]
       [:div.navbar-item
        [:a {:href "/users/signin"} [:i.fa.fa-user] "signin"]]]]]]

   [:div.main body]
   [:footer
    [:div.columns
     [:div "reviews.com"]]]])
