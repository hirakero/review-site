(ns isomorphic-clojure-webapp.boudary.users)

(defprotocol Users
  (get-user-by-id [db id])
  (create-user [db values]))