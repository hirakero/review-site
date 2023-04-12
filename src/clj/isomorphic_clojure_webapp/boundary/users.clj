(ns isomorphic-clojure-webapp.boundary.users)

(defprotocol Users
  (get-user-by-id [db id])
  (create-user [db values])
  (update-user [db id values])
  (delete-user [db id]))
