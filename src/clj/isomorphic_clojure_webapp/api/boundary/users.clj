(ns isomorphic-clojure-webapp.api.boundary.users
  (:require [buddy.hashers :as hashers]
            [duct.database.sql]
            [honey.sql :as sql]
            [honey.sql.helpers :as hh]
            [isomorphic-clojure-webapp.api.boundary.db-helper :as dbh])
  (:import (java.sql
            SQLException)))


(defprotocol Users

  (get-users [db])

  (get-user-by [db k v])

  (create-user [db values])

  (update-user [db id values])

  (delete-user [db id]))



(extend-protocol Users
  duct.database.sql.Boundary
  (get-users [db]
    (let [result (-> (hh/select :*)
                     (hh/from :users)
                     (sql/format)
                     (dbh/execute! db))
          sanitized-result (map #(dissoc % :password) result)]
      sanitized-result))

  (get-user-by [db k v]
    (let [value (if (= :id k) [:uuid v] v)
          result (-> (hh/select :*)
                     (hh/from :users)
                     (hh/where := k value)
                     (sql/format)
                     (dbh/execute-one! db))
          sanitized-result (dissoc result :password)]
      sanitized-result))

  (create-user [db {:keys [name email password]}]
    (let [hashed-password (hashers/encrypt password)
          result (-> (hh/insert-into :users [:name :email :password :created :updated])
                     (hh/values [[name email hashed-password [:now] [:now]]])
                     (sql/format)
                     #_(#((println "query " %) %))
                     (dbh/execute-one! db))
          sanitized-result (dissoc result :password)]
      sanitized-result))

  (update-user [db id values]
    (let [result (-> (hh/update :users)
                     (hh/set values)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (dbh/execute-one! db))
          sanitized-result (dissoc result :password)]
      sanitized-result))

  (delete-user [db id]
    (let [result (-> (hh/delete-from :users)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (dbh/execute-one! db))
          sanitized-result (dissoc result :password)]
      sanitized-result)))
