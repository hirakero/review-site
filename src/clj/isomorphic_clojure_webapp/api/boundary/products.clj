(ns isomorphic-clojure-webapp.api.boundary.products
  (:require [duct.database.sql]
            [honey.sql :as sql]
            [honey.sql.helpers :as hh]
            [isomorphic-clojure-webapp.api.boundary.db-helper :as dbh])
  (:import (java.sql
            SQLException)))

(defprotocol Products

  (get-products [db query])

  (get-product-by [db k v])

  (create-product [db values])

  (update-product [db id values])

  (delete-product [db id]))


(extend-protocol Products
  duct.database.sql.Boundary

  (get-products [db query]
    (let [result (-> (hh/select :*)
                     (hh/from :products)
                     (sql/format)
                     (dbh/execute! db))]
      result))

  (get-product-by [db k v]
    (let [value (if (= :id k) [:uuid v] v)
          query (-> (hh/select :*)
                    (hh/from :products)
                    (hh/where := k value)
                    (sql/format))]
      (dbh/execute-one! query db)))

  (create-product [db {:keys [name description]}]
    (let [result (-> (hh/insert-into :products [:name :description :created :updated])
                     (hh/values [[name description [:now] [:now]]])
                     (sql/format)
                     (dbh/execute-one! db))]
      result))

  (update-product [db id values]
    (let [result (-> (hh/update :products)
                     (hh/set values)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (dbh/execute-one! db))]
      result))

  (delete-product [db id]
    (let [result (-> (hh/delete-from :products)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (dbh/execute-one! db))]
      result)))
