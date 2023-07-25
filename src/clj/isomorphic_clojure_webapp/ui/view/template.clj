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
         [:a.pure-menu-link "main"]]
        [:li.pure-menu-item
         [:a.pure-menu-link "about"]]
        [:li.pure-menu-item
         [:a.pure-menu-link "contact"]]]]]
     [:a.pure-u-1-8.pure-button {:href "/users/signin"} "signin"]]]

   [:div.main body]
   [:footer
    [:div.pure-g
     [:divr.pure-u-1 "reviews.com"]]]])
