(ns slacky.accounts
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]))

(defn add-account! [db token key]
  (jdbc/with-db-transaction [db db]
    (let [result (first (jdbc/insert! db :accounts {:token token
                                                    :key key}))
          account-id (get result (keyword "last_insert_rowid()"))]
      (jdbc/insert! db :api_stats {:account_id account-id
                                   :hits 0}))))

(defn lookup-account [db token]
  (first (jdbc/query db (sql/format {:select [:*]
                                     :from [:accounts]
                                     :where [:= :token token]
                                     :limit 1}))))

(defn api-hit! [db ^Number account-id]
  (let [command "UPDATE api_stats SET hits = hits + 1 WHERE account_id = ?"]
    (jdbc/execute! db [command account-id])))

(defn api-stats [db account-id]
  (first (jdbc/query db (sql/format {:select [:hits]
                                     :from [:api_stats]
                                     :where [:= :account_id account-id]
                                     :limit 1}))))
