(ns micro-middleware.test.json-support
  (:use micro-middleware.json-support
        clojure.test
        ring.mock.request)
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]))

(deftest parse-accept-header-test
  (testing "application/json;level=1;q=1"
    (is (= {:type "application", :subtype "json"}
           (parse-accept-header "application/json;level=1;q=1"))))
  (testing "text/plain; q=0.5, text/html, text/x-dvi; q=0.8, text/x-c"
    (is (= {:type "text", :subtype "plain"}
           (parse-accept-header "text/plain; q=0.5, text/html, text/x-dvi; q=0.8, text/x-c"))))
  (testing "text/*, text/html, text/html;level=1, */*"
    (is (= {:type "text", :subtype "*"}
           (parse-accept-header "text/*, text/html, text/html;level=1, */*")))))

(deftest should-encode-to-json?-test
  (testing "should return true if client accepts json"
    (let [req (-> (request :get "/blah")
                  (header "Accept" "application/json"))]
      (is (should-encode-to-json? req))))
  (testing "should return false if client doesn't accept json"
    (let [req (-> (request :get "/blah")
                  (header "Accept" "text/plain"))]
      (is (not (should-encode-to-json? req)))))
  (testing "should return true if client doesn't care"
    (let [req (request :get "/blah")]
      (is (should-encode-to-json? req)))))

(deftest wrap-json-response-test
  (testing "should convert map to json string"
    (let [handler (fn [_] {:headers {} :body {:status :ok}})
          req (-> (request :get "/blah")
                  (header "Accept" "application/json"))
          res ((wrap-json-response handler) req)
          content-type (get-in res [:headers "Content-Type"])
          content-length (Integer/parseInt (get-in res [:headers "Content-Length"]))
          body (json/parse-stream (io/reader (:body res)) true)]
      (is (= "application/json; charset=utf8" content-type))
      (is (= 15 content-length))
      (is (= {:status "ok"} body))))
  (testing "should convert array to json string"
    (let [handler (fn [_] {:headers {}, :body [1, 2, 3]})
          req (-> (request :get "/blah")
                  (header "Accept" "application/json"))
          res ((wrap-json-response handler) req)
          content-type (get-in res [:headers "Content-Type"])
          content-length (Integer/parseInt (get-in res [:headers "Content-Length"]))
          body (json/parse-stream (io/reader (:body res)) true)]
      (is (= "application/json; charset=utf8" content-type))
      (is (= 7 content-length))
      (is (= [1, 2, 3] body))))
  (testing "should not alter response if client didn't ask for json"
    (let [handler (fn [_]
                    (let [body* (.getBytes "Blah" "utf8")]
                      {:headers {"Content-Type" "text/plain"
                                 "Content-Length" (str (count body*))}
                       :body (io/input-stream body*)}))
          req (-> (request :get "/blah")
                  (header "Accept" "text/plain"))
          res ((wrap-json-response handler) req)
          content-type (get-in res [:headers "Content-Type"])
          content-length (Integer/parseInt (get-in res [:headers "Content-Length"]))
          body (slurp (:body res))]
      (is (= "text/plain" content-type))
      (is (= 4 content-length))
      (is (= "Blah" body)))))

(deftest json-request?-test
  (testing "should return true if request content type is application/json"
    (let [body* (.getBytes (json/generate-string {:a 1, :b 2}) "utf8")
          req (-> (request :post "/blah")
                  (content-type "application/json")
                  (content-length (count body*))
                  (body body*))]
      (is (json-request? req))))
  (testing "should return false if request content type is not application/json"
    (let [req (-> (request :get "/blah")
                  (content-type "application/x-www-form-urlencoded"))]
      (is (not (json-request? req))))))

(deftest parse-json-params-test
  (testing "should parse json body to map"
    (let [fixture {:a 1, :b 2}
          body* (io/input-stream (.getBytes (json/generate-string fixture) "utf8"))
          json-params (parse-json-body body*)]
      (is (= fixture json-params))))
  (testing "should parse json body to array"
    (let [fixture [1, 2, 3]
          body* (io/input-stream (.getBytes (json/generate-string fixture) "utf8"))
          json-params (parse-json-body body*)]
      (is (= fixture json-params)))))

(deftest wrap-json-params-test
  (testing "should parse json body to map"
    (let [fixture {:a 1, :b 2}
          handler (fn [req] {:headers {}, :body (:body-params req)})
          body* (.getBytes (json/generate-string fixture) "utf8")
          req (-> (request :post "/blah")
                  (header "Accept" "application/json")
                  (content-type "application/json")
                  (content-length (count body*))
                  (body body*))
          res ((wrap-json-params handler) req)]
      (is (= fixture (:body res)))))
  (testing "should parse json body to array"
    (let [fixture [1, 2, 3]
          handler (fn [req] {:headers {}, :body (:body-params req)})
          body* (.getBytes (json/generate-string fixture) "utf8")
          req (-> (request :post "/blah")
                  (header "Accept" "application/json")
                  (content-type "application/json")
                  (content-length (count body*))
                  (body body*))
          res ((wrap-json-params handler) req)]
      (is (= fixture (:body res))))))