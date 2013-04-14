(ns micro-middleware.test.statistics-support
  (:use micro-middleware.statistics-support
        clojure.test
        ring.mock.request))

(deftest wrap-statistics-test
  (let [handler (fn [req]
                  (Thread/sleep 10)
                  {:headers {}, :body "ok"})]
    (testing "should capture statistics for different methods in single endpoint separately"
      ((wrap-statistics handler) (request :get "/foo"))
      ((wrap-statistics handler) (request :get "/foo"))
      ((wrap-statistics handler) (request :post "/foo"))
      ((wrap-statistics handler) (request :post "/foo"))
      ((wrap-statistics handler) (request :post "/foo"))
      (let [foo-get-stats (get-statistics ["/foo" :get])
            foo-post-stats (get-statistics ["/foo" :post])]
        (is (= (count (:raw foo-get-stats)) 2))
        (is (= (count (:raw foo-post-stats)) 3)))
      (reset-statistics ["/foo" :get])
      (reset-statistics ["/foo" :post]))
    (testing "should capture statistics for different endpoints separately"
      ((wrap-statistics handler) (request :get "/foo"))
      ((wrap-statistics handler) (request :get "/bar"))
      (let [foo-get-stats (get-statistics ["/foo" :get])
            bar-get-stats (get-statistics ["/bar" :get])]
        (is (= (count (:raw foo-get-stats)) 1))
        (is (= (count (:raw bar-get-stats)) 1)))
      (reset-statistics ["/foo" :get])
      (reset-statistics ["/bar" :get]))
    (testing "multithreaded"
      (let [t0 (future
                 ((wrap-statistics handler) (request :get "/foo"))
                 ((wrap-statistics handler) (request :post "/foo"))
                 ((wrap-statistics handler) (request :get "/bar"))
                 ((wrap-statistics handler) (request :post "/bar")))
            t1 (future
                 ((wrap-statistics handler) (request :post "/bar"))
                 ((wrap-statistics handler) (request :post "/foo"))
                 ((wrap-statistics handler) (request :get "/bar"))
                 ((wrap-statistics handler) (request :get "/foo")))]
        (deref t0)
        (deref t1)
        (is (= (count (:raw (get-statistics ["/foo" :get]))) 2))
        (is (= (count (:raw (get-statistics ["/foo" :post]))) 2))
        (is (= (count (:raw (get-statistics ["/bar" :get]))) 2))
        (is (= (count (:raw (get-statistics ["/bar" :post]))) 2))))))
