(ns isomorphic-clojure-webapp.api.boundary.reviews
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as hh]
            [isomorphic-clojure-webapp.api.boundary.db-helper :as dbh]))

(defprotocol Reviews
  (get-reviews [db query])

  (get-review-by-id [db id])

  (get-reviews-by-product [db product-id])

  (get-reviews-by-user [db user-id])

  (create-review [db values])

  (update-review [db id values])

  (delete-review [db id]))

(defn- p-long [x]
  (if (string? x) (Long/parseLong x) x))


(def max-limit 100)

;TODO camel-snake-kebabを使う？
(defn snake->kebab [k]
  (-> k
      name
      (clojure.string/replace #"_" "-")
      keyword))

(defn keys->kebab [m]
  (when (map? m)
    (reduce-kv (fn [m k v]
                 (assoc m (snake->kebab k) v))
               {}
               m)))

(defn list-map->kebab
  "マップのキーのキーワードに「＿」があれば「ー」に変換する
    [] 内のマップ再帰的に変換
    配列なら、firstを取って、restを再帰呼び出し
     マップならaccに追加   
   "
  [x]
  ;TODO すっきりさせる
  (loop [acc []
         v x]
    (cond
      (nil? v) nil
      (map? v)        (keys->kebab v)
      (empty? v) acc
      :default (recur
                (conj acc (keys->kebab (first v)))
                (rest v)))))
(comment
  #_[(= {:a-b 1} (list-map->kebab {:a_b 1}))
     (= [{:a-b 1}] (list-map->kebab [{:a_b 1}]))
     (= [{:a-b 1} {:c-d 2}] (list-map->kebab [{:a_b 1} {:c_d 2}]))
     (= [{:a-b 1} {:c-d 2} {:e-f 3}] (list-map->kebab [{:a_b 1} {:c_d 2} {:e_f 3}]))
     #_(nil? (list-map->kebab nil))])

(defn- get-reviews-by [[column id]]
  (->
   (hh/select :r.* [:u.name :user-name] [:p.name :product-name])
   (hh/from [:reviews :r])
   (hh/inner-join [:products :p] [:= :r.product-id :p.id])
   (hh/inner-join [:users :u]  [:= :r.user-id :u.id])
   (hh/where := column [:uuid id])))

(extend-protocol Reviews
  duct.database.sql.Boundary

  (get-reviews [db {:keys [limit offset sort order]}]
    (let [#_#_where (cond
                      name [:like :name (str "%" name "%")]
                      description [:like :description (str "%" description "%")]
                      :else [])
          lmt (let [l (p-long (or limit max-limit))]
                (if (< max-limit l)
                  max-limit
                  l))
          ofs (if offset (p-long offset) 0)
          result (-> (hh/select :*)
                     (hh/from :reviews)
                     #_(hh/where where)
                     (hh/offset ofs)
                     (hh/limit lmt)
                     (#(if-not (nil? sort)
                         (hh/order-by % [sort order])
                         %))
                     (sql/format)
                     #_((fn [s] (println "\nsql " s) (def *sql s) s))
                     (dbh/execute! db))]
      (list-map->kebab result)))
  #_(get-reviews [db {:keys [name description limit offset sort order]}]
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

  (get-review-by-id [db id]
    (let [result (-> (hh/select :*)
                     (hh/from :reviews)
                     (hh/where := :id [:uuid id])
                     (sql/format)
                     (dbh/execute-one! db))]
      (keys->kebab result)))

  (get-reviews-by-product [db product-id]
    (let [results (-> (hh/select :r.* [:u.name :user-name])
                      (hh/from [:reviews :r])
                      (hh/inner-join [:users :u]  [:= :r.user-id :u.id])
                      (hh/where := :r.product-id [:uuid product-id])
                      (sql/format)
                      #_((fn [s] (println "\nsql " s) (def *sql s) s))
                      (dbh/execute! db)
                      #_((fn [s] (println "\nresult " s) (def *results s) s)))]
      (list-map->kebab results)
      #_results))

  (get-reviews-by-user [db user-id]
    (let [results (-> (hh/select :r.* [:p.name :product-name])
                      (hh/from [:reviews :r])
                      (hh/inner-join [:products :p] [:= :r.product-id :p.id])
                      (hh/where := :user-id [:uuid user-id])
                      (sql/format)
                      #_((fn [s] (println "\nsql " s) (def *sql s) s))
                      (dbh/execute! db))]
      (list-map->kebab results)))

  (create-review [db {:keys [user-id product-id title content rate]}]
    (let [result (-> (hh/insert-into :reviews [:user-id :product-id :title :content :rate :created :updated])
                     (hh/values [[[:uuid user-id] [:uuid product-id] title content rate [:now] [:now]]])
                     (sql/format)
                     (dbh/execute-one! db))]
      (keys->kebab result)))

  (update-review [db id values]
    (let [values (assoc values :updated [:now])
          result (-> (hh/update :reviews)
                     (hh/set values)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (dbh/execute-one! db))]
      (keys->kebab result)))

  (delete-review [db id]
    (let [result (-> (hh/delete-from :reviews)
                     (hh/where [:= :id [:uuid id]])
                     (sql/format)
                     (dbh/execute-one! db))]
      (keys->kebab result))))