(defproject org.clojars.stask/micro-middleware "0.0.2"
  :description "Collection of middlewares"
  :url "http://github.com/stask/micro-middleware"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [cheshire "4.0.3"]
                 [ring/ring-core "1.1.6"]]
  :profiles {:dev
             {:dependencies [[ring-mock "0.1.3"]]}})