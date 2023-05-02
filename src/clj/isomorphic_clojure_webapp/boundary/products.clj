(ns isomorphic-clojure-webapp.boundary.products
  (:require [duct.database.sql]
            [honey.sql :as sql]
            [honey.sql.helpers :as hh]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (java.sql
            SQLException)))

(defprotocol Products

  (get-products [db query])

  (get-product-by [db k v])

  (create-product [db values])

  (update-product [db id values])

  (delete-product [db id]))


(defn- exec [f sql db]
  (let [ds  (-> db :spec :datasource)]
    (try
      (f ds sql
         {:return-keys true
          :builder-fn rs/as-unqualified-maps})
      (catch SQLException e
        (throw (ex-info "database error" {:query sql} e))))))

(defn execute-one! [sql db]
  (exec jdbc/execute-one! sql db))

(defn execute! [sql db]
  (exec jdbc/execute! sql db))

(extend-protocol Products
  duct.database.sql.Boundary

  (get-products [db query]
    (let [result (-> (hh/select :*)
                     (hh/from :products)
                     (sql/format)
                     (execute! db))]
      result))

  (get-product-by [db k v]
    (let [value (if (= :id k) [:uuid v] v)
          query (-> (hh/select :*)
                    (hh/from :products)
                    (hh/where := k value)
                    (sql/format))]
      (execute-one! query db)))

  (create-product [db {:keys [name description]}]
    (let [result (-> (hh/insert-into :products [:name :description :created :updated])
                     (hh/values [[name description [:now] [:now]]])
                     (sql/format)
                     (execute-one! db))]
      result))

  (update-product [db id values]
    (let [result (-> (hh/update :products)
                     (hh/set values)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (execute-one! db))]
      result))

  (delete-product [db id]
    (let [result (-> (hh/delete-from :products)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (execute-one! db))]
      result)))
