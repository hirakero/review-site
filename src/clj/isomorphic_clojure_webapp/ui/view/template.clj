(ns isomorphic-clojure-webapp.ui.view.template)

(defn template [& body]
  [[:header
    [:h1 "reviews.com"]
    [:div.pure-g
     [:div.pure-u-7-8
      [:div.pure-menu.pure-menu-horizontal
       [:div.pure-menu-heading "menu"]
       [:ul.pure-menu-list
        [:li.pure-menu-item
         [:a.pure-menu-link {:href "/"} "main"]]
        [:li.pure-menu-item
         [:a.pure-menu-link {:href "/about"} "about"]]
        [:li.pure-menu-item
         [:a.pure-menu-link {:href "/contact"} "contact"]]]]]
     [:a.pure-u-1-8.pure-button {:href "/users/signin"} "signin"]]

    [:div.pure-g
     [:div.pure-u-1-4]
     [:form.pure-u-1-2 {:method :get :action ""}
      [:div.pure-g
       [:input.pure-u-5-8 {:type :text :id "q" :name "q"}]
       [:div.pure-u-1-8]
       [:button.pure-u-1-4 {:type :submit} "search"]]]
     [:div.pure-u-2-8]]]

   [:div.main body]
   [:footer
    [:div.pure-g
     [:divr.pure-u-1 "reviews.com"]]]])
