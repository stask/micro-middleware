(ns micro-middleware.test.timer-support
  (:use micro-middleware.timer-support
        clojure.test
        ring.mock.request))

(deftest wrap-measure-time-test
  (testing "should add elapsed time to response headers"
    (let [handler (fn [req] {:headers {}, :body ""})
          req (request :get "/blah")
          res ((wrap-measure-time handler) req)]
      (is (not (nil? (get-in res [:headers "X-Elapsed"])))))))