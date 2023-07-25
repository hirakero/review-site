(ns isomorphic-clojure-webapp.ui.view.template)

(defn template [& body]
  [[:div.header
    [:h1 "reviews.com"]
    [:div.pure-g
     [:div.pure-u-7-8 ""
      [:div.pure-menu.pure-menu-horizontal
       [:div.pure-menu-heading "menu"]
       [:ul.pure-menu-list
        [:li.pure-menu-item
         [:a.pure-menu-link "main"]]
        [:li.pure-menu-item
         [:a.pure-menu-link "about"]]
        [:li.pure-menu-item
         [:a.pure-menu-link "contact"]]]]]
     [:a.pure-u-1-8.pure-button {:href "#"} "signin"]]]

   #_[:div.
      [:div.pure-g]
      [:div.title.pure-u-1 "title"]
      [:div.pure-g]
      [:div.pure-menu.pure-menu-horizontal
       [:div.pure-menu-heading "menu"]
       [:ul.pure-menu-list
        [:li.pure-menu-item
         [:a.pure-menu-link "main"]]
        [:li.pure-menu-item
         [:a.pure-menu-link "about"]]
        [:li.pure-menu-item
         [:a.pure-menu-link "contact"]]]]
      [:div.pure-g
       [:div.pure-u-3-4 ""]
       [:div.button.pure-u-1-4 "signin"]]]
   [:div.main body]
   [:div.footer]])
