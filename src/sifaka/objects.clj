(ns sifaka.objects
  (:require (sifaka [object-common :as com])))

(defn container
  ^{:doc "Testing: fudge a container with optional contents."}
  [{[x y] :position
    [width height] :size
    [r g b] :colour} & contents]
  (com/window (merge com/fudged-common {:class "Container"
                                        :text "Container"
                                        :x x
                                        :y y
                                        :width width
                                        :height height
                                        :color (com/colour r g b)
                                        :label 1
                                        :tabbar 1
                                        :meta 0})
              contents))

(defn pads
  ^{:doc "Testing: fudge a single button (1x1 pad)."}
  [{id :id
    name :name
    [x y] :position
    [width height] :size
    [r0 g0 b0] :off-colour
    [r1 g1 b1] :on-colour}]
  (com/window {:class "Pads"
               :id id
               :text name
               :x x
               :y y
               :width width
               :height height
               :row 1
               :column 1
               :capture 1
               :font "tahoma,10,0"
               :label 0
               :state 1
               :group 0
               :send 1
               :osc_target -2
               :midi_target -2
               :kbmouse_target -2
               :color (format "%d,%d"
                              (com/colour r0 g0 b0)
                              (com/colour r1 g1 b1))
               :multicolor 0
               :multilabel 0}
              (cons
               (com/param name "x" 0.0)
               (com/env name {}))))

(defn ringarea
  ^{:doc "RingArea control."}
  [{id :id
    name :name
    [x y] :position
    size :size
    [r g b] :colour}]
  (let [params [(com/param name "x" 0.5)
                (com/param name "y" 0.5)]
        variables [(com/variable name "attraction" 1.0)
                   (com/variable name "friction" 0.8)
                   (com/variable name "speed" 1.0)
                   (com/variable name "attraction_x" 0.5)
                   (com/variable name "attraction_y" 0.5)]]
    (com/window {:class "RingArea"
                 :id id
                 :text name
                 :x x
                 :y y
                 :width size
                 :height size
                 :label 0
                 :state 1
                 :group 0
                 :send 1
                 :capture 1
                 :font "tahoma,10,0"
                 :osc_target -2
                 :midi_target -2
                 :kbmouse_target -2
                 :color (com/colour r g b)}
                (concat params variables))))
