(ns scramblies.core)

(defn scramble? [str1 str2]
  (let [fr1 (frequencies str1)
        fr2 (frequencies str2)]
    (every? (fn [[ch cnt2]]
              (when-let [cnt1 (get fr1 ch)]
                (>= cnt1 cnt2))) fr2)))
