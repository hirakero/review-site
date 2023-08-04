(ns isomorphic-clojure-webapp.ui.handler.common
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.ui.response :refer [ok]]
            [isomorphic-clojure-webapp.ui.view.template :refer [template]]
            [isomorphic-clojure-webapp.ui.boundary.products :as products]))

(defmethod ig/init-key ::top [_ {:keys [host]}]
  (fn [{qp :query-params}]
    (apply ok (template
               [:div.box
                [:div.is-size-4 "rate ranking"]
                (for [i (range 1 6)]
                  [:div.box
                   [:div.is-size-5  (str i "位: " "product name(link)")]
                   [:img {:src ""}]
                   [:div "rate"]])]

               [:div.box
                [:div.is-size-4 "recent comment"]
                (for [i (range 1 6)]
                  [:div.box
                   [:div.is-size-5 "product name(link)"]
                   [:img {:src ""}]
                   [:div "user name"]
                   [:div "comment (link)"]])]

               [:div.box
                [:div.field
                 [:div.is-size-4 "new products"]]
                (let [{:keys [body]} (products/get-products host {:limit 5})]
                  (for [p (:products body)]
                    [:div.box
                     [:div.is-size-5 [:a {:href "#"} (:name p)]]
                     [:img {:src ""}]
                     [:small (:updated p)]]))]))))

(defmethod ig/init-key ::about [_ _]
  (fn [_]
    (apply ok (template
               [:h2 "about"]))))
(defmethod ig/init-key ::contact [_ _]
  (fn [_]
    (apply ok (template
               [:h2 "contact"]))))
