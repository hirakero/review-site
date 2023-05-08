(ns isomorphic-clojure-webapp.boundary.users
  (:require [buddy.hashers :as hashers]
            [duct.database.sql]
            [honey.sql :as sql]
            [honey.sql.helpers :as hh]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (java.sql
            SQLException)))


(defprotocol Users

  (get-users [db])

  (get-user-by [db k v])

  (create-user [db values])

  (update-user [db id values])

  (delete-user [db id]))


(def ^:private execute-opts
  {:return-keys true
   :builder-fn rs/as-unqualified-maps})

(defn- get-datasource [d]
  (-> d :spec :datasource))

(defn- execute-one! [sql db]
  (let [ds (get-datasource db)]
    (jdbc/execute-one! ds sql execute-opts)))

(defn- execute! [sql db]
  (let [ds (get-datasource db)]
    (jdbc/execute! ds sql execute-opts)))

(extend-protocol Users
  duct.database.sql.Boundary
  (get-users [db]
    (let [result (-> (hh/select :*)
                     (hh/from :users)
                     (sql/format)
                     (execute! db))
          sanitized-result (map #(dissoc % :password) result)]
      sanitized-result))

  (get-user-by [db k v]
    (let [value (if (= :id k) [:uuid v] v)
          result (-> (hh/select :*)
                     (hh/from :users)
                     (hh/where := k value)
                     (sql/format)
                     (execute-one! db))
          sanitized-result (dissoc result :password)]
      sanitized-result))

  (create-user [db {:keys [name email password]}]
    (try
      (let [hashed-password (hashers/encrypt password)
            result (-> (hh/insert-into :users [:name :email :password :created :updated])
                       (hh/values [[name email hashed-password [:now] [:now]]])
                       (sql/format)
                       #_(#((println "query " %) %))
                       (execute-one! db))
            sanitized-result (dissoc result :password)]
        sanitized-result)
      (catch SQLException e
        (throw e)
        ;; TODO log
        ;; TODO ex-info
        )))

  (update-user [db id values]
    (let [result (-> (hh/update :users)
                     (hh/set values)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (execute-one! db))
          sanitized-result (dissoc result :password)]
      sanitized-result))

  (delete-user [db id]
    (let [result (-> (hh/delete-from :users)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (execute-one! db))
          sanitized-result (dissoc result :password)]
      sanitized-result)))
