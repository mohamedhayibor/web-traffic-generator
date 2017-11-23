(ns web-traffic-generator.core
  (:require [clj-http.client :as client]
            [clojure.set :as s]))

;;  -- Simple web traffic generator
;;  -- Mimicks a user browsing the internet randomly
;;  -- 1. randomly clicks a page
;;  -- 2. randomly "backspace" rage back to start url (needs debugging)
;;  --> The program crashes when it hits a 4** status code

(def links (atom {:current-link ""
                  :back-log #{}
                  :walked #{}
                  :block-list #{"facebook.com" "https://facebook.com"}}))


(defn get-dom-page
  "Makes an http request -> html dom"
  [url-link]
  (:body (client/get url-link)))


(defn get-page-links
  "Gets all links from the requested page"
  [dom-page]
  (let [reg #"(?:href\=\")(https?:\/\/[^\"]+)(?:\")"
        re-seq-ed (re-seq reg dom-page)
        lazy-links (map second re-seq-ed)]
    (into #{} lazy-links)))


(defn append-to-backlog!
  [page-links]
  (swap! links assoc :back-log page-links))


(defn filter-links
  "filter out walked and blocked links"
  []
  (let [walked-set (:walked @links)
        blocked-list (:block-list @links)
        back-log (:back-log @links)
        links-diff (s/difference back-log walked-set blocked-list)]
    (into [] links-diff)))


(defn web-traffic-generator
  "Kicks the web traffic generator when provided with a valid link"
  [url]
  (let [dom (get-dom-page url)
        page-links (get-page-links dom)
        back-log (append-to-backlog! page-links)
        f-links (filter-links)]
    (prn ">>>>>>>> X debug")
    (prn f-links)
    (rand-nth f-links)))


(defn time-stamp!
  "Logs the current link, then puts into walked"
  [current-link]
  (println "Current link: " (:current-link @links))
  (swap! links assoc :walked (conj (:walked @links) current-link)))


(defn -main
  "runs the program, ctrl + c to exit"
  [link]
  (prn "program starts")
  (swap! links assoc :current-link link)
  (loop []
    (time-stamp! (:current-link @links))
    (Thread/sleep (* (rand-int 6) 1000))
    (swap! links assoc :current-link (web-traffic-generator (:current-link @links)))
    (recur)))


(-main "https://news.ycombinator.com")
