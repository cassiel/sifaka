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
               [:PARAM (merge {:name "x="
                               :value 0.0
                               :send 17
                               :midi_scale "0,16363"
                               :osc_scale "0.0,1.0"}
                              (com/base-osc-params name "x")
                              com/fudge-midi-params     ; Actually, midi_trigger=1 here.
                              com/fudge-kbmouse-params)]
               (com/env name {}))))
