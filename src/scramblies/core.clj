(ns scramblies.core
  (:require [org.httpkit.server :as server]
            [reitit.ring :as ring]
            [reitit.coercion.spec :as re-spec]
            [reitit.ring.coercion :as rrc]
            [reitit.coercion :as coercion]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.dev.pretty :as pretty]
            [muuntaja.core :as m]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))

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
                   :scramblies/two]))

(defn build-error-message [spec]
  {:type "error"
   :message (format "Invalid value %s via: %s"
                    (:value spec)
                    (->> spec
                         :problems
                         (mapcat :via)
                         (str/join " ")))})

(defn coercion-error-handler [status]
  (let [handler (exception/create-coercion-handler status)]
    (fn [exception request]
      (let [resp (handler exception request)]
        (if (get-in resp [:body :problems])
          (assoc resp :body (build-error-message (:body resp)))
          resp)))))

(def handler
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/scramble" {:parameters {:query ::scrabmle-query-params}
                   :get        {:handler scramble-handler}}]]
    {:data {:muuntaja   m/instance
            :exception  pretty/exception
            :coercion   re-spec/coercion
            :compile    coercion/compile-request-coercers
            :middleware [muuntaja/format-middleware
                         parameters/parameters-middleware
                         rrc/coerce-exceptions-middleware
                         (exception/create-exception-middleware
                             (merge
                               exception/default-handlers
                               {:reitit.coercion/request-coercion (coercion-error-handler 400)
                                :reitit.coercion/response-coercion (coercion-error-handler 500)}))
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
             :query-params   {:one1 "hello" :two "world"}})
   (m/decode-response-body))
;; => {:type "error",
;;     :message
;;     "Invalid value {:one1 \"hello\", :two \"world\"} via: :scramblies.core/scrabmle-query-params"}

  )
