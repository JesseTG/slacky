(ns dev
  (:require [slacky
             [service :as service]
             [server :as server]
             [settings :as settings]]
            [io.pedestal.http :as bootstrap]
            [clojure.tools.namespace.repl :refer [refresh]]
            [slacky.db :as db]))

(def service (-> service/service ;; start with production configuration
                 (merge  {:env :dev
                          ;; do not block thread that starts web server
                          ::bootstrap/join? false
                          ;; reload routes on every request
                          ::bootstrap/routes #(deref #'service/routes)
                          ;; all origins are allowed in dev mode
                          ::bootstrap/allowed-origins (constantly true)})
                 (bootstrap/default-interceptors)
                 (bootstrap/dev-interceptors)))

(defn start [& [opts]]
  (server/create-server (merge service opts))
  (bootstrap/start server/service-instance))

(defn stop []
  (when server/service-instance
    (bootstrap/stop server/service-instance)))
