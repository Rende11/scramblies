(ns ui.core
  (:require
   [ajax.core :refer [GET]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [clojure.string :as str]))

(defonce app-state
  (r/atom {:form-state :normal}))

(defn sanitize-input [s]
  (-> s
      str/trim
      str/lower-case))

(defn check-scrumble [one* two*]
  (let [one (sanitize-input one*)
        two (sanitize-input two*)]
    (GET "http://localhost:8080/api/scramble"
         {:params          {:one one
                            :two two}
          :handler         (fn [resp]
                             (swap! app-state assoc :result (:result resp) :form-state :success))
          :error-handler   (fn [err]
                             (prn "Error " err)
                             (swap! app-state assoc :result "Error, try again later" :form-state :error))})))

(defn change-handler [e path]
  (swap! app-state assoc-in path (.. e -target -value)))

(def path-one [:form :input :one])
(def path-two [:form :input :two])

(defn results-map [result]
  (case result
    true  ["right" "Yes!"]
    false ["wrong" "No ("]
    ["error"]))

(defn app []
  (let [{:keys [form-state result]} @app-state
        [res-class res-msg]         (results-map result)]
    [:div.inner-app
     [:form.scramble-form {:class     form-state
                           :on-submit #(do
                                         (.preventDefault %)
                                         (check-scrumble
                                          (get-in @app-state path-one)
                                          (get-in @app-state path-two)))}
      [:div.inputs
       [:input.form-item.inp.inp-one {:required    true
                                      :type        :text
                                      :placeholder "Place word one here..."
                                      :on-change   #(change-handler % path-one)}]

       [:input.form-item.inp.inp-two {:required    true
                                      :type        :text
                                      :placeholder "Place word two here..."
                                      :on-change   #(change-handler % path-two)}]]

      [:input.form-item.send {:type  :submit
                              :value "Scramble?"}]

      [:div.form-item.result {:class res-class}
       (or res-msg result)]]]))


(defn ^:dev/after-load start []
  (rdom/render [app] (js/document.getElementById "app")))

(defn init []
  (start))
