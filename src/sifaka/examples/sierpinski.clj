(ns sifaka.examples.sierpinski)

(def template
  [[0 0] [1 0] [2 0]
   [0 1] [2 1]
   [0 2] [1 2] [2 2]])

(defn sierpinski
  ^{:doc "Sierpinski as a replicating generator: deeper generations
          replicate a pattern with higher X and Y."}
  [depth]
  (if (= depth 1)
    {:points template
     :scale 3}
    (let [{:keys [points scale]}
          (sierpinski (dec depth))]
      {:points
       (apply concat (map (fn [[tx ty]] (map (fn [[x y]] [(+ x (* tx scale))
                                                        (+ y (* ty scale))]) points))
                          template))
       :scale (* scale 3)})))
