(ns isomorphic-clojure-webapp.ui.handler.common
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.ui.response :refer [ok see-other]]
            [isomorphic-clojure-webapp.ui.view.template :refer [template]]))


(defmethod ig/init-key ::top [_ {:keys [host]}]
  (fn [{qp :query-params}]
    (apply ok (template
               [:div
                [:h3 "ranking"]
                (for [i (range 1 6)]
                  [:div
                   [:div (str i "位")]
                   [:img {:src ""}]
                   [:div  "name link"]])]

               [:div
                [:h3 "recent"]
                (for [i (range 1 6)]
                  [:div
                   [:div "product name(link)"]
                   [:img {:src ""}]
                   [:div "date"]
                   [:div "user name(link)"]])]
               [:div
                [:h3 "new item"]
                (for [_ (range 5)]
                  [:div
                   [:div "product name(link)"]
                   [:img {:src ""}]
                   [:div "date"]
                   [:div "title"]])]))))

(defmethod ig/init-key ::about [_ _]
  (fn [_]
    (apply ok (template
               [:h2 "about"]))))
(defmethod ig/init-key ::contact [_ _]
  (fn [_]
    (apply ok (template
               [:h2 "contact"]))))