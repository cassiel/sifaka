(ns user
  (:require (sifaka [io :as io]
                    [util :as u]
                    [packets :as p]
                    [object-common :as com]
                    [objects :as obj]
                    [xml-fu :as x])
            (sifaka.examples [sierpinski :as sp])
            (clojure [prxml :as px]))
  (:import [java.nio.channels DatagramChannel]
           [java.nio ByteBuffer]
           [java.net InetAddress InetSocketAddress DatagramSocket DatagramPacket]))

(def LEMUR "192.168.85.141")

;; --- Basic XML generation.

(with-out-str (px/prxml [:p] [:q]))

(with-out-str (px/prxml [:p {:class "greet"} [:i "Ladies & gentlemen"]]))

(x/format-project-for-file [:fooble] [:gooble])

(x/format-project-for-upload [:fooble] [:gooble])

(with-out-str (p/prxml (x/project "Hello World")))

(with-out-str (p/prxml (x/fudge-container [100 100] [200 50] [80 80 80] [:foo])))

;; Nested container example. (container() takes varargs for the contents.)

(defn boz [size]
  (if (< size 30)
    nil
    (obj/container {:position [0 0]
                    :size [size size]
                    :colour [80 80 80]}
                   (boz (- size 25)))))

(boz 200)

(io/transmit-payload
 LEMUR
 8002
 (.getBytes (x/format-project-for-upload
             (x/project "TestProject")
             (x/interface [(obj/container
                            {:position [100 100]
                             :size [500 500]
                             :colour [80 120 120]}
                            (boz 600))]))))

(sp/sierpinski 3)

;; More examples.

(io/transmit-payload
 LEMUR
 8002
 (.getBytes (x/format-project-for-upload
             (x/project "TestProject")
             (x/interface [(obj/container
                            {:position [100 100]
                             :size [300 300]
                             :colour [80 120 120]}
                            (obj/pads {:id 987
                                       :name "pads"
                                       :position [0 0]
                                       :size [284 284]
                                       :off-colour [50 50 50]
                                       :on-colour [255 255 255]}))]))))

(io/transmit-payload
 LEMUR
 8002
 (.getBytes (x/format-project-for-upload
             (x/project "TestProject")
             (x/interface [(obj/container
                            {:position [100 100]
                             :size [300 300]
                             :colour [80 120 120]}
                            (obj/ringarea {:id 987
                                           :name "ra"
                                           :position [0 0]
                                           :size 284
                                           :colour [0 150 150]}))]))))

(io/transmit-payload
 LEMUR
 8002
 (.getBytes (x/format-project-for-upload
             (x/project "TestProject")
             (x/interface [(obj/container
                            {:position [10 10]
                             :size [700 700]
                             :colour [80 120 120]}
                            (map #(obj/ringarea
                                   {:id %
                                    :name (format "ra%d" %)
                                    :position [(int (* 278 (inc (Math/sin (* % Math/PI 0.125)))))
                                               (int (* 278 (inc (Math/cos (* % Math/PI 0.125)))))]
                                    :size 120
                                    :colour [(- 255 (* % 10)) 200 150]}) (range 16)))]))))

(int (/ (- 1024 (+ 16 (* 25 27))) 2))

(* 27 9)

(- 27 1 1 )

(let [{:keys [scale points]} (sp/sierpinski 3)
      button-size 25
      buttons-pitch (* button-size scale)
      container-pitch (+ 16 buttons-pitch)
      x-pos (int (/ (- 1024 container-pitch) 2))]

  (io/transmit-payload
   LEMUR
   8002
   (.getBytes
    (x/format-project-for-upload
     (x/project "TestProject")
     (x/interface
      [(obj/container
        {:position [x-pos 5]
         :size [(+ 16 (* scale button-size)) (+ 16 (* scale button-size))]
         :colour [100 100 100]}
        (for [[xx yy] points]
          (let [c (if (even? (+ xx yy))
                    [(* yy 9) (* xx 9) 255]
                    [255 (* (- scale xx 1) 9) (* (- scale yy 1) 9)])]
            (obj/pads {:id (+ xx (* yy scale))
                       :name "MyButton"
                       :position [(* xx button-size) (* yy button-size)]
                       :size [button-size button-size]
                       :off-colour c
                       :on-colour [255 0 0]}))))])))))

(e e e e  e e e )

;; -----

(obj/pads {:id 345
           :name "Hello"
           :position [50 50]
           :size [200 200]
           :off-colour [0 0 0]
           :on-colour [1 1 1]})

(with-out-str (px/prxml (obj/pads {:id 345
                                   :name "Hello"
                                   :position [50 50]
                                   :size [200 200]
                                   :off-colour [0 0 0]
                                   :on-colour [1 1 1]})))


(com/env "my-obj" {})
