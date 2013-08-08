(defproject stask/micro-middleware "0.0.12"
  :description "Collection of middlewares"
  :url "http://github.com/stask/micro-middleware"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]
                 [ring/ring-core "1.2.0"]
                 [clj-time "0.5.1"]]
  :profiles {:dev
             {:dependencies [[ring-mock "0.1.5"]]}})
