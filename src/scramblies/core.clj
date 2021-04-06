(ns scramblies.core
  (:require [org.httpkit.server :as server]
            [reitit.ring :as ring]
            [reitit.coercion.spec :as re-spec]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [clojure.spec.alpha :as s]))

(defonce server (atom nil))

(defn scramble? [str1 str2]
  (let [fr1 (frequencies str1)
        fr2 (frequencies str2)]
    (every? (fn [[ch cnt2]]
              (when-let [cnt1 (get fr1 ch)]
                (>= cnt1 cnt2))) fr2)))

(defn scramble-handler [req]
  (let [{:keys [one two]} (get-in req [:parameters :query])
        result            (scramble? one two)]
    {:status 200
     :body   {:result result}}))

(s/def ::ne-string
  (every-pred string? not-empty))

(s/def :scramblies/one ::ne-string)
(s/def :scramblies/two ::ne-string)

(s/def ::scrabmle-query-params
  (s/keys :req-un [:scramblies/one
                   :scramblies/two] ))

(def handler
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/scramble" {:parameters {:query ::scrabmle-query-params}
                   :get        {:handler scramble-handler}}]]
    {:data {:muuntaja   m/instance
            :coercion   re-spec/coercion
            :middleware [muuntaja/format-middleware
                         parameters/parameters-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware]}})))


(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server []
  (reset! server (server/run-server #'handler {:port 8080})))



(comment
  (start-server)
  (stop-server)

  (->
   (handler {:request-method :get
             :uri            "/api/scramble"
             :query-params   {:one "hello" :two "heoll"}})
   m/decode-response-body)


  )
