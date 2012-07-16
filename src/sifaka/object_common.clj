(ns sifaka.object-common)

(defn colour [r g b]
  (+ (bit-shift-left r 16)
     (bit-shift-left g 8)
     b))

(def fudged-common
  ^{:doc "Fudge: some parameters we can treat as common, for now."}
  {:state 1
   :group 0
   :font "tahoma,10,0"
   :send 1
   :osc_target -2
   :midi_target -2
   :kbmouse_target -2})

(def fudge-midi-params
  ^{:doc "Default - possibly disabled - MIDI settings."}
  {:midi_target -1
   :midi_trigger -1
   :midi_message (format "0x%02x,0x%02x,0,0" 0x90 0x90)
   :midi_scale (format "%d,%d" 0 0x3FFF)})

(def fudge-kbmouse-params
  ^{:doc "Default - possibly disabled - keyboard/mouse settings."}
  {:kbmouse_target -1
   :kbmouse_trigger 1
   :kbmouse_message "0,0,0"
   :kbmouse_scale "0,1,0,1"})

(defn base-osc-params
  ^{:doc "Basic OSC parameters - somewhat cargo-culted."}
  [object-name var-name]
  {:osc_target 0
   :osc_trigger 1
   :osc_message (format "/%s/%s" object-name var-name)})

(defn variable
  ^{:doc "Common settings for an object variable. Value assumed to be float."}
  [object-name var-name var-value]
  [:VARIABLE (merge {:name (format "%s=%f" var-name var-value)
                     :send 0}
                    (base-osc-params object-name var-name)
                    fudge-midi-params
                    fudge-kbmouse-params)])

(defn param
  ^{:doc "Common settings for an object parameter."}
  [obj-name param-name value]
  [:PARAM (merge {:name (format "%s=" param-name)
                  :value value
                  :send 17 ; Is this the trigger mode?
                  :midi_scale "0,16363"
                  :osc_scale "0.0,1.0"}
                 (base-osc-params obj-name param-name)
                 fudge-midi-params
                 fudge-kbmouse-params)])

(defn window
  ^{:doc "Window phrase, with attributes (map) and sub-items (seq)."}
  [attributes sub-items]
  (vec (concat [:WINDOW attributes]
               sub-items)))

(defn env
  ^{:doc "ADSR/H envelope settings. Takes a map keyed with :a :d :s :r :h,
          defaults any missing values (sustain 1.0, all others 0.0).
          Returns a sequence of VARIABLE nodes."}
  [object-name values]
  (let [all-values (merge {:a 0.0 :d 0.0 :s 1.0 :r 0.0 :h 0.0} values)
        var-names {:a "attack"
                   :d "decay"
                   :s "sustain"
                   :r "release"
                   :h "hold"}]
    (map #(variable object-name (var-names %) (all-values %)) [:a :d :s :r :h])))
