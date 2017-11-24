(ns web-traffic-generator.core
  (:require [clj-http.client :as client]
            [clojure.set :as s]))

;;  -- Simple web traffic generator
;;  -- Mimicks a user browsing the internet randomly
;;
;;  -- Sets are heavily used as data structures as
;;  -- we don't care too much of order in any kind

(def links (atom {:current-link ""
                  :back-log #{}
                  :walked #{}
                  :block-list #{"facebook.com"
                                "https://facebook.com"
                                "https://static.xx.fbcdn.net/"}}))


(defn get-dom-page
  "Gets html from url as one string"
  [url-link]
  (:body (client/get url-link)))


(defn get-page-links
  "Gets all links from page then puts them in a set"
  [dom-page]
  (let [reg #"(?:href\=\")(https?:\/\/[^\"]+)(?:\")"
        re-seq-ed (re-seq reg dom-page)
        lazy-links (map second re-seq-ed)]
    (into #{} lazy-links)))


(defn append-to-backlog!
  "Inserts all new links into backlog"
  [page-links]
  (swap! links assoc :back-log
                     (s/union (:back-log @links) page-links)))


(defn filter-links
  "Filter out walked and blocked links"
  []
  (let [walked-set (:walked @links)
        blocked-list (:block-list @links)
        back-log (:back-log @links)
        links-diff (s/difference back-log walked-set blocked-list)]
    (into [] links-diff)))


(defn web-traffic-generator
  "Perform the main operations when provided with a valid link"
  [url]
  (let [dom (get-dom-page url)
        page-links (get-page-links dom)
        back-log (append-to-backlog! page-links)
        f-links (filter-links)]
    ;; (prn ">>>>>>>> X debug")
    ;; (prn f-links)
    (rand-nth f-links)))


(defn time-stamp!
  "Logs the current link, then puts into walked"
  [current-link]
  (println "Current link: " (:current-link @links))
  (swap! links assoc :walked
                     (conj (:walked @links) current-link)))


(defn -main
  "runs the program, ctrl + c to exit"
  [link]
  (prn "program starts")
  (swap! links assoc :current-link link)
  (loop []
    (time-stamp! (:current-link @links))
    (Thread/sleep (* (rand-int 16) 1000))
    (swap! links assoc :current-link
                       (web-traffic-generator (:current-link @links)))
    (recur)))


;; (-main "https://news.ycombinator.com")
