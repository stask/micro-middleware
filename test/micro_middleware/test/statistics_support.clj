(ns micro-middleware.test.statistics-support
  (:use micro-middleware.statistics-support
        clojure.test
        ring.mock.request))

(deftest wrap-statistics-test
  (testing "should capture statistics for different endpoints"
    (let [handler (fn [req]
                    (Thread/sleep 10)
                    {:headers {}, :body "ok"})
          req (request :get "/foo")
          res ((wrap-statistics handler) req)]
      (prn res))))
