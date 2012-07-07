(ns sifaka.test.packet-tests
  (:require [sifaka [packets :as p]])
  (:import [java.nio ByteBuffer])
  (:use [expectations]))

(defn explode [ba] (map (partial get ba)
                        (range (count ba))))

(expect [65 66 0 0]
        (explode (p/pad-string-to-bytes "AB")))

(expect [0 0 2 16
         (int \/) (int \j) (int \z) (int \m)
         (int \l) 0 0 0
         (int \,) (int \b) 0 0
         0 0 1 -3]
        (explode (p/header 509)))

(expect [[0 1 2] [3 4 5]]
        (map explode
             (p/payload-packets' 3 (ByteBuffer/wrap (byte-array (map byte (range 6)))) 0 nil)))

(expect [[0 1 2 3 4] [5]]
        (map explode
             (p/payload-packets' 5 (ByteBuffer/wrap (byte-array (map byte (range 6)))) 0 nil)))

(expect [[0 0 0 24
          (int \/) (int \j) (int \z) (int \m)
          (int \l) 0 0 0
          (int \,) (int \b) 0 0
          0 0 0 5]
         [1 2 3 4 5 0 0 0]]
        (map explode (p/package-data (byte-array (map byte [1 2 3 4 5])))))

(expect [[0 0 0 28
          (int \/) (int \j) (int \z) (int \m)
          (int \l) 0 0 0
          (int \,) (int \b) 0 0
          0 0 0 9]
         [1 2 3 4 5 6 7 8 9 0 0 0]]
        (map explode (p/package-data (byte-array (map byte [1 2 3 4 5 6 7 8 9])))))
