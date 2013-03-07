(defproject org.clojars.stask/micro-middleware "0.0.5"
  :description "Collection of middlewares"
  :url "http://github.com/stask/micro-middleware"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [cheshire "5.0.2"]
                 [ring/ring-core "1.1.6"]]
  :profiles {:dev
             {:dependencies [[ring-mock "0.1.3"]]}})
