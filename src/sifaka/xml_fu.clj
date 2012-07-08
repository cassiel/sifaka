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

(def fudged-common
  ^{:doc "Fudge: some parameters we can treat as common, for now."}
  {:state 1
   :group 0
   :font "tahoma,10,0"
   :send 1
   :osc_target -2
   :midi_target -2
   :kbmouse_target -2})

(defn colour [r g b]
  (+ (bit-shift-left r 16)
     (bit-shift-left g 8)
     b))

(defn fudge-container
  ^{:doc "Testing: fudge a container with optional contents."}
  [{[x y] :position
    [width height] :size
    [r g b] :colour} & contents]
  [:WINDOW (merge fudged-common {:class "Container"
                                 :text "Container"
                                 :x x
                                 :y y
                                 :width width
                                 :height height
                                 :color (colour r g b)
                                 :label 1
                                 :tabbar 1
                                 :meta 0}) contents])

(defn fudge-osc-params
  [object-name var-name]
  {:osc_target 0
   :osc_trigger 1
   :osc_message (format "/%s/%s" object-name var-name)})

(def fudge-midi-params
  {:midi_target -1
   :midi_trigger -1
   :midi_message (format "0x%02x,0x%02x,0,0" 0x90 0x90)
   :midi_scale (format "%d,%d" 0 0x3FFF)})

(def fudge-kbmouse-params
  {:kbmouse_target -1
   :kbmouse_trigger 1
   :kbmouse_message "0,0,0"
   :kbmouse_scale "0,1,0,1"})

(defn fudge-var
  ^{:doc "Fudge common settings for an object variable."}
  [object-name var-name var-value]
  [:VARIABLE (merge {:name (format "%s=%d" var-name var-value)
                     :send 0}
                    (fudge-osc-params object-name var-name)
                    fudge-midi-params
                    fudge-kbmouse-params)])

(defn fudge-button
  ^{:doc "Testing: fudge a single button (1x1 pad)."}
  [{id :id
    name :name
    [x y] :position
    [width height] :size
    [r0 g0 b0] :off-colour
    [r1 g1 b1] :on-colour}]
  [:WINDOW {:class "Pads"
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
                           (colour r0 g0 b0)
                           (colour r1 g1 b1))
            :multicolor 0
            :multilabel 0}
   [:PARAM (merge {:name "x="
                   :value 0.0
                   :send 17
                   :midi_scale "0,16363"
                   :osc_scale "0.0,1.0"}
                  (fudge-osc-params name "x")
                  fudge-midi-params     ; Actually, midi_trigger=1 here.
                  fudge-kbmouse-params)]
   (fudge-var name "attack" 0)
   (fudge-var name "decay" 0)
   (fudge-var name "sustain" 1)
   (fudge-var name "release" 0)
   (fudge-var name "hold" 0)
   (fudge-var name "light" 0)])
