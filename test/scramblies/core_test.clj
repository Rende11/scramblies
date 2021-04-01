(ns scramblies.core-test
  (:require  [clojure.test :refer [deftest testing is]]
             [scramblies.core :refer [scramble?]]))

(deftest basic-test
  (testing "matched"
    (is (scramble? "rekqodlw" "world"))
    (is (scramble? "cedewaraaossoqqyt" "codewars")))

  (testing "not matched"
    (is (false? (scramble? "katas" "steak")))))
