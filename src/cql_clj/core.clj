(ns cql-clj.core
  (:require
   [clj-antlr.core :as antlr]
   [clojure.pprint :as pprint]
   [clojure.java.io :as io]))

(def parser (antlr/parser (.getPath (io/resource "cql_clj/cql.g4"))))

(spit "/tmp/res.edn" 
      (with-out-str
        (pprint/pprint
         (parser
          (slurp (io/resource "cql_clj/sample.cql"))))))
