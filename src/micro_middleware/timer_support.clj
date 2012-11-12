(ns micro-middleware.timer-support
  (:require [ring.util.response :as response]))

(defn wrap-measure-time
  [handler]
  (fn [req]
    (let [started-at (System/nanoTime)
          res (handler req)
          elapsed (/ (- (System/nanoTime) started-at) 1000000.0)]
      (response/header res "X-Elapsed" elapsed))))