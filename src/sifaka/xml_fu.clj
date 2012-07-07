(ns sifaka.xml-fu
  (:require (clojure [prxml :as p])))

(defn format-project-for-file
  ^{:doc "Generate XML for a project to write to a JZML file."}
  [& project-xml]
  (with-out-str (p/prxml [:JZML project-xml])))

(defn format-project-for-upload
  ^{:doc "Generate XML for a project to encase in an upload."}
  [& project-xml]
  (with-out-str
    (p/prxml [:RESET]
             [:OSC {:request 1}]
             [:SYNCHRO {:mode 0}]
             project-xml)))

(defn project
  ^{:doc "Project header. For now I'm cargo-culting the size and targets."}
  [title]
  [:PROJECT {:title title
             :version "3020"
             :width 1024
             :height 724
             :osc_target -2
             :midi_target -2
             :kbmouse_target -2}])

(defn fudge-window
  ^{:doc "Testing: fudge a single window"}
  [contents]
  [:WINDOW {:class "JAZZINTERFACE"
            :text "c"
            :x 0
            :y 0
            :width 1024
            :height 724
            :state 1
            :group 0
            :font "tahoma,11,0"}
   contents])

(defn fudge-container
  ^{:doc "Testing: fudge a container with optional contents."}
  [[x y] [width height] [r g b] & contents]
  [:WINDOW {:class "Container"
            :text "Container"
            :x x
            :y y
            :width width
            :height height
            :state 1
            :group 0
            :font "tahoma,10,0"
            :send 1
            :osc_target -2
            :midi_target -2
            :kbmouse_target -2
            :color (+ (bit-shift-left r 16)
                      (bit-shift-left g 8)
                      b)
            :label 1
            :tabbar 1
            :meta 0} contents])
