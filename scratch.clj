(ns user
  (:require (sifaka [io :as io]
                    [util :as u])
            (clojure [prxml :as p]))
  (:use overtone.osc)
  (:use overtone.osc.encode)
  (:use overtone.osc.peer))

(with-out-str (p/prxml [:p]))

(with-out-str (p/prxml [:p {:class "greet"} [:i "Ladies & gentlemen"]]))

;; Do everything in bytes; the Lemur's OSC spec uses blobs, and I doubt it's going
;; to handle any non-ASCII encoding such as UTF-8.




(count ba)

(def xxx (byte-array (map byte  [2 3 4])))

(def yyy (byte-array 10))

(System/arraycopy xxx 0 yyy 0 (count xxx))

(map #(get yyy %) (range (count yyy)))





(def a (atom nil))

(def LEMUR_HOST "localhost")
(def LEMUR_HOST "10.0.0.125")
(def LEMUR_PORT 8001)

; loopback test.
(def server (osc-server 8001))
(osc-handle server "/interface" (fn [msg] (reset! a msg)))
(osc-close server)

(def client (osc-client LEMUR_HOST LEMUR_PORT))

(def ba (io/read-file "test-data/tiny.jzml"))

(osc-send client
          "/interface"
          (count ba)
          (int 0)
          (int (u/crc ba))
          ba)

@a
