(defproject cql-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [cheshire "5.6.3"]
                 [clj-time "0.12.0"]
                 [json-schema "0.1.1"]
                 [mpg "1.3.0"]
                 [clj-yaml "0.4.0"]
                 [clj-antlr "0.2.4"]
                 [org.clojure/core.memoize "0.5.9"]
                 ;; ;; Logging
                 ;; [org.clojure/tools.logging "0.3.1"]
                 ;; [org.slf4j/slf4j-api "1.7.21"]

                 [org.clojure/tools.logging "0.3.1"]]
  :profiles {:dev {:dependencies [;; [ch.qos.logback/logback-classic "1.1.7"]
                                  [org.postgresql/postgresql "9.4.1211.jre7"]
                                  [org.clojure/java.jdbc "0.6.1"]
                                  [com.velisco/herbert "0.7.0"]
                                  [backtick "0.3.3"]
                                  [clj-pg "0.0.1-RC2"]
                                  [functionalbytes/mount-lite "0.9.8"]
                                  [ch.qos.logback/logback-classic "1.1.7"]
                                  [matcho "0.1.0-RC3"]
                                  ;; json schema dev deps
                                  [http-kit "2.1.19"]]}})
