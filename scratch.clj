(ns user
  (:require (sifaka [io :as io]
                    [util :as u]
                    [format :as f])
            (clojure [prxml :as p]))
  (:use overtone.osc)
  (:use overtone.osc.encode)
  (:use overtone.osc.peer)
  (:import [java.nio.channels DatagramChannel])
  (:import [java.nio ByteBuffer])
  (:import [java.net InetSocketAddress]))


;; --- Basic XML generation.

(with-out-str (p/prxml [:p]))

(with-out-str (p/prxml [:p {:class "greet"} [:i "Ladies & gentlemen"]]))

;; Do everything in bytes; the Lemur's OSC spec uses blobs, and I doubt it's going
;; to handle any non-ASCII encoding such as UTF-8.


(def xxx (byte-array (map byte  [2 3 4])))

(def yyy (byte-array 10))

(System/arraycopy xxx 0 yyy 0 (count xxx))

(map #(get yyy %) (range (count yyy)))

;; --- OSC experiments (obsolete: the Lemur protocol isn't OSC).

(def a (atom nil))

(def LEMUR_HOST "localhost")
(def LEMUR_HOST "10.0.0.125")
(def LEMUR_PORT 8001)

; loopback test.
(def server (osc-server 8001))
(osc-handle server "/interface" (fn [msg] (reset! a msg)))
(osc-close server)

(def client (osc-client LEMUR_HOST LEMUR_PORT))

(osc-send client
          "/interface"
          (count ba)
          (int 0)
          (int (u/crc ba))
          ba)

@a

;; --- XML file into byte-array.

(def ba (io/read-file "test-data/tiny.jzml"))
(count ba)

;; --- Bespoke, Lemur-specific broken OSC:

;; Header is 20 bytes / 5 int32s.
;; 0:           padded payload length + 16
;; 1+2:         "/jzml" padded
;; 3:           ",b" padded
;; 4:           unpadded payload length
;;
;; Actual (padded) payload is sent "raw", in packets limited to 1448 bytes.


(def HEADER_SIZE_BYTES 20)



(def chan (DatagramChannel/open))
(.connect chan (InetSocketAddress. "10.0.0.125" 8002))



(.write chan (ByteBuffer/wrap (byte-array (map byte [1 2 3 4]))))



(.disconnect chan)

(let
    [ba (io/pad-string-to-bytes "AB")]
  (map (partial get ba) (range (count ba))))

(int \c)

(map count
     (f/package-data (byte-array (map byte [1 2 3 4 5]))))
