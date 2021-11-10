(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.java.io :as io]
            [clojure.tools.build.api :as b]
            [org.corfield.build :as bb])
  (:import [org.apache.avro.compiler.specific SpecificCompiler]))

(def default-protocol-filename "resources/avro/Pizza Delivery.avpr")
(def default-generated-java-dir "src/main/generated-java")

(defn generate-java-from-avro-protocol
  [{:keys [protocol-filename generated-java-dir] :as opts}]
  (println (format "Generating Java source from Avro protocol \"%s\" to directory \"%s\"" protocol-filename generated-java-dir))
  (SpecificCompiler/compileProtocol (io/file protocol-filename)
                                    (io/file generated-java-dir))
  opts)

(defn compile-java
  [{:keys [basis class-dir target java-src-path] :as opts}]
  (println (format "Compiling Java source in \"%s\"" java-src-path))
  (b/javac {:src-dirs   [java-src-path]
            :class-dir  (bb/default-class-dir class-dir target)
            :basis      (bb/default-basis basis)
            :javac-opts ["-source" "8" "-target" "8"]})
  opts)

(def lib 'com.onote.example.pizza-delivery/clojure-cljfx-ui)
(def version "0.1.0-SNAPSHOT")
(def main 'com.onote.example.pizza-delivery-ui)

(def clean bb/clean)

(defn compile-java-from-avro-protocol
  [opts]
  (let [generated-java-dir default-generated-java-dir]
    (generate-java-from-avro-protocol
     (assoc opts
            :protocol-filename default-protocol-filename
            :generated-java-dir generated-java-dir))
    (compile-java
     (assoc opts
            :java-src-path generated-java-dir))
    opts))

(defn test "Run the tests."
  [opts]
  (compile-java-from-avro-protocol opts)
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
        (compile-java-from-avro-protocol)
        (bb/run-tests)
        (bb/clean)
        (bb/uber))))
