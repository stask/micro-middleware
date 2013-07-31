(ns micro-middleware.json-support
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [ring.util.response :as response])
  (:import (java.util.zip GZIPInputStream)))

;; Poor man Accept header parsing.
;; Proper parsing should implement http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
;; We only take first type.
(defn parse-accept-header
  [accept-header]
  (if (nil? accept-header)
    {:type "*", :subtype "*"}
    (let [parsed-header (s/split (s/trim accept-header) #";")
          first-type (first (s/split (s/trim (first parsed-header)) #","))
          [type subtype] (s/split (s/trim first-type) #"/")]
      {:type type, :subtype subtype})))

(defn should-encode-to-json?
  [req res]
  (let [accept-header (get-in req [:headers "accept"])
        {:keys [type subtype]} (parse-accept-header accept-header)]
    (and
     (coll? (:body res))
     (or (= "application" type) (= "*" type))
     (or (= "json" subtype) (= "*" subtype)))))

(defn wrap-json-response
  [handler & {:keys [dehyphenize]}]
  (fn [req]
    (let [{:keys [headers body] :as res} (handler req)]
      (if (should-encode-to-json? req res)
        (let [json-opts (if dehyphenize
                          {:key-fn #(s/replace (name %) #"-" "_")}
                          {})
              body* (.getBytes (json/generate-string body json-opts) "utf8")]
          (-> (assoc res :body (io/input-stream body*))
              (response/content-type "application/json; charset=utf8")
              (response/header "Content-Length" (count body*))))
        res))))

(defn json-request?
  [req]
  (let [content-type (:content-type req)]
    (and (not (nil? content-type))
         (not-empty (re-find #"^application/(?:vnd.+)?json" content-type)))))

(defn compressed?
  [req]
  (if-let [content-encoding (get-in req [:headers "content-encoding"])]
    (not (nil? (re-find #"(?i)gzip" content-encoding)))
    false))

(defn parse-json-body
  [body decompress hyphenize]
  (json/parse-stream (io/reader (if decompress
                                  (GZIPInputStream. body)
                                  body))
                     (if hyphenize
                       #(keyword (s/replace % #"_" "-"))
                       keyword)))

(defn wrap-json-params
  [handler & {:keys [hyphenize]}]
  (fn [req]
    (handler (if (json-request? req)
               (assoc req :body-params (parse-json-body (:body req)
                                                        (compressed? req)
                                                        hyphenize))
               req))))
