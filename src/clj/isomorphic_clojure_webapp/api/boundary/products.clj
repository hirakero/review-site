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

(defn- p-long [x]
  (if (string? x) (Long/parseLong x) x))

(def max-limit 100)
(extend-protocol Products
  duct.database.sql.Boundary

  (get-products [db {:keys [name description limit offset sort order]}]
    (let [where (cond
                  name [:like :name (str "%" name "%")]
                  description [:like :description (str "%" description "%")]
                  :else [])
          lmt (if limit
                (let [l (p-long limit)]
                  (if (< max-limit l)
                    max-limit
                    l))
                max-limit)
          ofs (if offset (p-long offset) 0)
          result (-> (hh/select :*)
                     (hh/from :products)
                     (hh/where where)
                     (hh/offset ofs)
                     (hh/limit lmt)
                     (#(if-not (nil? sort)
                         (hh/order-by % [sort order])
                         %))
                     (sql/format)
                     #_((fn [s] (println "\nsql " s) (def *sql s) s))
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
    (let [values (assoc values :updated [:now])
          result (-> (hh/update :products)
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
