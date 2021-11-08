(ns com.onote.example.pizza-delivery-ui
  (:gen-class)
  (:require [integrant.core :as ig]
            [com.onote.example.pizza-delivery-ui.config :as config]
            [io.pedestal.log :as log]))

(set! *warn-on-reflection* true)

(defn set-default-uncaught-exception-handler!
  "Sets the default uncaught exception handler to log the error and exit
  the JVM process."
  []
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread ex]
       (log/error :exception ex :thread thread)
       (System/exit 1)))))

(defn add-shutdown-hook!
  "Adds a shutdown hook that gracefully stops the running system."
  [f]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(do
                                (log/info :application :stop)
                                (f)))))

(defn -main []
  (log/info :application :starting)
  (let [system (ig/init (config/config))]
    (add-shutdown-hook! #(do
                           (log/info :application :stopping)
                           (ig/halt! system)
                           (shutdown-agents)))
    (set-default-uncaught-exception-handler!)
    (log/info :application :started)))
