(ns sifaka.util)

(defn padded-size
  ^{:doc "4-byte aligned size of a byte-array (overtone.osc.encode sort-of has one already)."}
  [n]
  (+ n (- 3 (mod (dec n) 4))))

(defn try-nth
  ^{:private true}
  [ba pos]
  (if
      (>= pos (count ba))
    0
    (get ba pos)))

(defn crc-long
  ^{:doc "CRC for the word at pos, allowing for out-of-bounds."
    :private true}
  [ba pos]
  (+ (try-nth ba pos)
     (bit-shift-left (try-nth ba (+ 1 pos)) 8)
     (bit-shift-left (try-nth ba (+ 2 pos)) 16)
     (bit-shift-left (try-nth ba (+ 3 pos)) 24)))

(defn crc1
  ^{:doc "CRC iterator; not protected against int32 overflow."
    :private true}
  [ba pos result]
  (let
      [c (- (count ba) pos)]
    (if (<= c 0)
      result
      (recur ba (+ pos 4) (+ result (crc-long ba pos)))
      )))

(defn crc
  ^{:doc "Checksum of byte array."}
  [ba]
  (bit-and 0x7FFFFFFF
           (crc1 ba 0 0)))
