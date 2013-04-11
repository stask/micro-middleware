(ns micro-middleware.statistics-support)

(defn wrap-statistics
  [handler]
  (fn [{:keys [uri request-method] :as req}]
    (let [started-at (System/nanoTime)
          res (handler req)
          elapsed (/ (- (System/nanoTime) started-at) 1000000.0)]
      (println "Request" (str (list uri request-method)) "took" elapsed))))
