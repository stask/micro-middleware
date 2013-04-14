(defproject stask/micro-middleware "0.0.8"
  :description "Collection of middlewares"
  :url "http://github.com/stask/micro-middleware"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.1.1"]
                 [ring/ring-core "1.1.8"]]
  :profiles {:dev
             {:dependencies [[ring-mock "0.1.3"]]}})
