(ns isomorphic-clojure-webapp.boundary.users 
  (:require [duct.database.sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as hh]
            [buddy.hashers :as hashers])
  (:import [java.sql SQLException]))

(defprotocol Users
  (get-users [db])
  (get-user-by-id [db id])
  (create-user [db values])
  (update-user [db id values])
  (delete-user [db id]))

(def ^:private execute-opts {:return-keys true
                             :builder-fn rs/as-unqualified-maps})

(extend-protocol Users
  duct.database.sql.Boundary
  (get-users [db]
    (let [ds (-> db :spec :datasource)
          result (jdbc/execute! ds
                                ["select * from users"]
                                execute-opts)
          sanitized-result (map #(dissoc % :password) result)]
      sanitized-result))

  (get-user-by-id [db id]
    (let [ds (-> db :spec :datasource)
          result (jdbc/execute-one! ds
                                (sql/format (-> (hh/select :*)
                                                (hh/from :users)
                                                (hh/where := :id id)))
                                execute-opts)
          sanitized-result (dissoc result :password)]
      sanitized-result))
  
    (create-user [db {:keys [name email password]}] 
      (try 
        (let [ds (-> db :spec :datasource)  
              hashed-password (hashers/encrypt password)
              sql (sql/format (-> (hh/insert-into :users [:name :email :password])
                                  (hh/values [[name email hashed-password]]))) 
              result (jdbc/execute-one! ds 
                                        sql 
                                        execute-opts) 
              sanitized-result (dissoc result :password)]
          sanitized-result) 
        (catch SQLException e 
          (throw e)
          ;TODO log
          ;TODO ex-info  
          )))

  (update-user [db id values]
    (let [ds (-> db :spec :datasource)
          result (jdbc/execute-one! ds
                                    (sql/format (-> (hh/update :users)
                                                    (hh/set values)
                                                    (hh/where [:= :id id])))
                                    execute-opts)
          sanitized-result (dissoc result :password)]
      sanitized-result))

  (delete-user [db id]
    (let [ds (-> db :spec :datasource)
          result (jdbc/execute-one! ds
                                (sql/format (-> (hh/delete-from :users)
                                                (hh/where [:= :id id])))
                                execute-opts)
          sanitized-result (dissoc result :password)]
      sanitized-result)))