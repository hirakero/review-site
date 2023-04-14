(ns isomorphic-clojure-webapp.boundary.users 
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import [java.sql SQLException]))

(defprotocol Users
  (get-user-by-id [db id])
  (create-user [db values])
  (update-user [db id values])
  (delete-user [db id]))

(def ^:private execute-opts {:return-keys true
                             :builder-fn rs/as-unqualified-maps})

(extend-protocol Users
  duct.database.sql.Boundary
  (get-user-by-id [db id]                  )
  (create-user [db values] 
    (try
      (let [ds (-> db :spec :datasource)
            result (jdbc/execute! ds ["INSERT INTO users (name) VALUES (?)" (:name values)] execute-opts)]
        result)
      (catch SQLException e 
        (do
          ;TODO log
          {:errors [{:code 2001 :message (.getMessage e)}]})))) 
  
  (update-user [db id values])
  (delete-user [db id])
  )