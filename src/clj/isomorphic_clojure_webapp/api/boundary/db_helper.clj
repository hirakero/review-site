(ns isomorphic-clojure-webapp.api.boundary.db-helper
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (java.sql
            SQLException)))

(defn- exec [f sql db]
  (let [ds  (-> db :spec :datasource)]
    (try
      (f ds sql
         {:return-keys true
          :builder-fn rs/as-unqualified-maps})
      (catch SQLException e
        (throw (ex-info (str "database error: " (.getMessage e)) {:query sql} e))))))

(defn execute-one! [sql db]
  (exec jdbc/execute-one! sql db))

(defn execute! [sql db]
  (exec jdbc/execute! sql db))
