(ns isomorphic-clojure-webapp.ui.boundary.users
  (:require [isomorphic-clojure-webapp.ui.boundary.http-helper :as helper]))

(defprotocol Users

  (get-users [this query])

  (get-user-by-id [this id])

  (create-user [this values])

  (update-user [this id values])

  (delete-user [this id])

  (signin [this values]))



(extend-protocol Users
  isomorphic_clojure_webapp.ui.boundary.web.Boundary

  (get-users [{:keys [base-url]} query]
    (let [query-string (->> (keys query)  (map #(str (name %) "=" (get query %))) (clojure.string/join "&"))]
      (helper/http-get (str base-url "/api/users" (when-not (empty? query-string) (str "?" query-string))))))

  (get-user-by-id [{:keys [base-url]} user-id]
    (helper/http-get (str base-url "/api/users/" user-id)))

  (create-user [{:keys [base-url]} values]
    (helper/http-post (str base-url "/api/users") values))

  (update-user [{:keys [base-url]} id values]
    (helper/http-put (str base-url "/api/users/" id) values))

  (delete-user [{:keys [base-url]} user-id]
    (helper/http-delete (str base-url "/api/users/" user-id)))

  (signin [{:keys [base-url]} values]
    (helper/http-post (str base-url "/api/signin") values)))