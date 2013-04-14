(ns micro-middleware.statistics-support)

(def ^:private statistics (atom {}))

(defn get-statistics
  ([] (let [current-statistics @statistics]
        (map (fn [[key statistics]] {:key key :statistics (get-statistics key statistics)}))))
  ([key] (get-statistics key @(get @statistics key)))
  ([key {:keys [start end raw]}]
     (let [elapsed (- end start)
           n (count raw)]
       {:start start
        :end end
        :elapsed elapsed
        :rps (if (> elapsed 0) (/ n (/ elapsed 1000.0)) n)
        :avg (/ (reduce + raw) n)
        :min (apply min raw)
        :max (apply max raw)
        :raw raw})))

(defn reset-statistics
  ([] (reset! statistics (atom {})))
  ([key] (swap! statistics dissoc key)))

(defn wrap-statistics
  [handler]
  (fn [{:keys [uri request-method] :as req}]
    (let [started-at (System/nanoTime)
          res (handler req)
          elapsed (/ (- (System/nanoTime) started-at) 1000000.0)
          current-time (System/currentTimeMillis)
          stats-key (list uri request-method)]
      (swap! statistics #(if (contains? % stats-key) %
                             (assoc % stats-key (atom {:start current-time
                                                       :raw '()}))))
      (swap! (get @statistics stats-key) #(-> %
                                              (assoc :end current-time)
                                              (update-in [:raw] conj elapsed)))
      res)))
