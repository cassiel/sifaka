(ns sifaka.examples.sierpinski
  (:require (sifaka [io :as io]
                    [xml-fu :as xml])))

(defn sierpinski
  ^{:doc "Return list of X/Y position and size of squares."}
  [depth [x y] size]
  (if (= depth 0)
    nil
    (conj (remove nil? (map (fn [xy]
                              (sierpinski
                               (dec depth)
                               (map (partial * size) xy)
                               (/ size 3)))
                            [[-1/3 1/3] [0 1/3] [1/3 1/3]
                             [-1/3 0] [1/3 0]
                             [-1/3 -1/3] [0 -1/3] [1/3 -1/3]]))
          [[x y] size])))
