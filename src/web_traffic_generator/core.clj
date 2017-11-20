(ns web-traffic-generator.core
  (:gen-class))

;;  -- Simple web traffic generator
;;  -- Mimicks a user browsing the internet randomly.

(def current-link (atom {:link ""}))

(defn get-dom-page
  "Makes an http request -> html dom"
  [url-link]
  (slurp url-link))


(defn get-all-links
  "Gets all links from the requested page"
  [dom-page]
  (mapv second (re-seq #"(?:href\=\")(https?:\/\/[^\"]+)(?:\")" dom-page)))


(defn pick-random-link
  "Picks a random link out of all in the page"
  [all-links]
  (if (nil? all-links)
    (do
      (print "No links to be found. Exiting...")
      (System/exit 0))
    (rand-nth all-links)))


(defn web-traffic-generator
  "Kicks the web traffic generator when provided a valid link"
  [url]
  (if (nil? url)
    (do
      (print "No links to be found. Exiting...")
      (System/exit 0))
    (-> (get-dom-page url)
        get-all-links
        pick-random-link)))


;; -- Other spec details
;; TODO: implement block list > filter already searched out link
(defn -main
  "runs the program, ctrl + c to exit"
  [link]
  (prn "program starts")
  (swap! current-link assoc :link link)
  (loop []
    (println "current link: " (:link @current-link))
    (Thread/sleep (* (rand-int 6) 1000))
    (swap! current-link assoc :link (web-traffic-generator (:link @current-link)))
    (recur)))

;; (-main "https://news.ycombinator.com")
