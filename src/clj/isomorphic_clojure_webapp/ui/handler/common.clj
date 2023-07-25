(ns isomorphic-clojure-webapp.ui.handler.common
  (:require [integrant.core :as ig]
            [isomorphic-clojure-webapp.ui.response :refer [ok see-other]]
            [isomorphic-clojure-webapp.ui.view.template :refer [template]]))


(defmethod ig/init-key ::top [_ {:keys [host]}]
  (fn [{qp :query-params}]
    (apply ok (template
               [:h2 "ranking"]
               [:ul
                [:li "1位 name link"]
                [:li "2位"]
                [:li "3位"]
                [:li "4位"]
                [:li "5位"]]

               [:h2 "recent"]
               [:ul
                [:li "1位 name link"]
                [:li "2位"]
                [:li "3位"]
                [:li "4位"]
                [:li "5位"]]
               [:h2 "new item"]
               [:ul
                [:li "name 2023.7.7"]
                [:li "name 2023.7.7"]
                [:li "name 2023.7.7"]
                [:li "name 2023.7.7"]
                [:li "name 2023.7.7"]]))))

