(ns sifaka.test.util-tests
  (:require [sifaka [util :as u]])
  (:use [expectations]))

(expect 0 (u/padded-size 0))
(expect 4 (u/padded-size 1))
(expect 4 (u/padded-size 3))
(expect 4 (u/padded-size 4))
(expect 8 (u/padded-size 5))

(expect 0x04030201
        (u/crc (byte-array (map byte [1 2 3 4]))))

(expect 0x0605
        (u/crc (byte-array (map byte [5 6]))))

(expect (+ 0x0A090807 0x0C0B)
        (u/crc (byte-array (map byte [7 8 9 10 11 12]))))

;; Test for overflow:

(expect 0x00000000
        (u/crc (byte-array (map byte [0 0 0 -0x80 0 0 0 -0x80]))))
