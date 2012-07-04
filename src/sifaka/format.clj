(ns sifaka.format
  (:require [sifaka [util :as util]])
  (:import [java.nio ByteBuffer ByteOrder]))

(defn pad-string-to-bytes
  ^{:doc "Pad a string to 4-byte boundary, return as a byte array."}
  [str]
  (let
      [buff (ByteBuffer/allocate (util/padded-size (count str)))]
    (.array (.put buff (.getBytes str)))))

(def HEADER-SIZE-BYTES 20)

(defn header
  ^{:doc "Create the header packet."}
  [payload-len]
  (let
      [padded-len (util/padded-size payload-len)
       buff (.order (ByteBuffer/allocate HEADER-SIZE-BYTES) ByteOrder/BIG_ENDIAN)]
    (-> buff
        ;; Int giving the total padded size of all packets, minus the int itself:
        (.putInt (+ padded-len (- HEADER-SIZE-BYTES 4)))
        ;; Padded string "/jzml":
        (.put (pad-string-to-bytes "/jzml"))
        ;; Padded OSC-style "blob" string:
        (.put (pad-string-to-bytes ",b"))
        ;; Int giving size of unpadded payload:
        (.putInt payload-len)
        (.array))))

(def MAX-BLOCK-SIZE 1448)

(defn payload-packets'
  ^{:doc "Return a sequence of payload packets from a payload ByteBuffer."}
  [block-size payload pos result]
  (let [lim (.limit payload)]
    (if (= pos lim)
      (reverse result)
      (let
          [packet-size (min (- lim pos) block-size)
           ba (byte-array packet-size)]
        (recur block-size
               (.get payload ba)
               (+ pos packet-size)
               (cons ba result))))))

(defn payload-packets [payload]
  (payload-packets' MAX-BLOCK-SIZE payload 0 nil))

(defn pad-bytebuffer
  ^{:doc "Return a ByteBuffer over a byte array, correctly padded."}
  [ba]
  (-> (ByteBuffer/allocate (util/padded-size (count ba)))
      (.put ba)
      (.rewind)))

(defn package-data
  ^{:doc "Package a byte array into a sequence of data blocks, the first being the header."}
  [payload]
  (cons (header (count payload))
        (payload-packets (pad-bytebuffer payload))))
