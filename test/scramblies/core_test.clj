(ns scramblies.core-test
  (:require  [clojure.test :refer [deftest testing is]]
             [scramblies.core :refer [scramble? handler]]
             [muuntaja.core :as m]))

(defn get-resp [req-opts]
  (let [resp (handler req-opts)
        body* (m/decode-response-body resp)]
    (assoc resp :body body*)))

(deftest basic-test
  (testing "matched"
    (is (true? (scramble? "rekqodlw" "world")))
    (is (true? (scramble? "cedewaraaossoqqyt" "codewars"))))

  (testing "not matched"
    (is (false? (scramble? "katas" "steak")))))


(deftest endpoint-test
  (testing "matched"
    (is (= (-> (get-resp {:request-method :get
                          :uri            "/api/scramble"
                          :query-params   {:one "rekqodlw" :two "world"}})
               (select-keys [:status :body]))
           {:body   {:result true}
            :status 200}))

    (is (= (-> (get-resp {:request-method :get
                          :uri            "/api/scramble"
                          :query-params   {:one "cedewaraaossoqqyt" :two "codewars"}})
               (select-keys [:status :body]))
           {:body   {:result true}
            :status 200})))

  (testing "not matched"
    (is (= (-> (get-resp {:request-method :get
                          :uri            "/api/scramble"
                          :query-params   {:one "katas" :two "steak"}})
               (select-keys [:status :body]))
           {:body   {:result false}
            :status 200})))

  (testing "wrong request"
    (is (= (-> (get-resp {:request-method :get
                          :uri            "/api/scramble"
                          :query-params   {:unknown "katas" :two "steak"}})
               (select-keys [:status :body]))
           {:status 400,
            :body
            {:type "error",
             :message "Invalid value {:unknown \"katas\", :two \"steak\"} via: :scramblies.core/scrabmle-query-params"}}))))
