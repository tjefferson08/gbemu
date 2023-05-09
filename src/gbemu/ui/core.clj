(ns gbemu.ui.core
  (:require [gbemu.bus :as bus]))

(defn tile-px [ctx {:keys [tile-num x y addr scale]}]
  (let [tile-line (fn [b1 b2 tile-y]
                     (map (fn [bit]
                            (let [hi    (if (bit-test b2 bit) 2r10 2r00)
                                  lo    (if (bit-test b1 bit) 2r01 2r00)
                                  color (bit-or hi lo)
                                  x'     (+ x (* (- 7 bit) scale))
                                  y'     (+ y (* tile-y scale))]
                                  ;; _     (println (format (str "bytes 1:%02X 2:%02X color: " color " hi: " hi " lo: " lo " x, y: " x "," y) b1 b2))]
                               {:color color, :x x', :y y'}))
                          (range 0 8)))]
    (mapcat (fn [line] (let [tile-y (* 2 line)
                             b1     (bus/read! ctx (+ addr (* tile-num 16) tile-y))
                             b2     (bus/read! ctx (+ addr (* tile-num 16) (inc tile-y)))]
                          (tile-line b1 b2 line)))
         (range 0 8))))

(defn all-tile-px
  ([ctx scale addr] (all-tile-px ctx scale addr 364))
  ([ctx scale addr n-tiles]
   (->> (for [x (range 16)
              y (range 24)]
          [(+ x (* y 16)) (* 8 scale x) (* 8 scale y)])
        (sort-by first)
        (map (fn [[n x y]] (tile-px ctx {:tile-num n, :x x, :y y, :addr addr, :scale scale})))
        (take n-tiles)
        flatten)))

(comment
  (def px (for [x (range 16)
                y (range 2)
                scale [4]]
            (let [offset-x (* 7 scale x)
                  offset-y (* 7 scale y)]
              [(+ x (* y 16)) (* 8 scale x) (+ offset-y (* scale y))])))

  (sort-by first px)

 ,,,)
