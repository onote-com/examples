(ns build
  (:refer-clojure :exclude [test])
  (:require [org.corfield.build :as bb]))

(def lib 'com.onote.example.pizza-delivery/clojure-cljfx-ui)
(def version "0.1.0-SNAPSHOT")
(def main 'com.onote.example.pizza-delivery-ui)

(def clean bb/clean)

(defn test "Run the tests."
  [opts]
  (bb/run-tests opts))

(defn ci "Run the CI pipeline of tests (and build the uberjar)."
  [opts]
  (let [target (bb/default-target)]
    (-> opts
        (assoc :lib lib
               :version version
               :main main
               :target target
               :uber-file (bb/default-jar-file (str target "/uberjar/") lib version))
        (bb/run-tests)
        (bb/clean)
        (bb/uber))))
