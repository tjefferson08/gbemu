(ns gbemu.ui.graphics-2d-example
  (:import [java.awt Color Graphics Graphics2D Dimension]
           [java.awt.image BufferedImage]
           [javax.swing JFrame JPanel])
  (:require [gbemu.log :as log]
            [gbemu.state :refer [*ctx]]
            [gbemu.ui.core :as ui]
            [gbemu.bus :as bus]))

; embedded nREPL
(require '[nrepl.server :refer [start-server stop-server]])
(defonce server (start-server :port 7889))

;; static unsigned long tile_colors[4] = {0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000};
(def TILE_COLORS [(new Color 0xFFFFFF) (new Color 0xAAAAAA)
                  (new Color 0x555555) (new Color 0)])

(def TEST_SPRITE [{:color 0, :x 0, :y 0}
                  {:color 2, :x 4, :y 0}
                  {:color 3, :x 8, :y 0}
                  {:color 3, :x 12, :y 0}
                  {:color 3, :x 16, :y 0}
                  {:color 3, :x 20, :y 0}
                  {:color 2, :x 24, :y 0}
                  {:color 0, :x 28, :y 0}

                  {:color 0, :x 0, :y 4}
                  {:color 3, :x 4, :y 4}
                  {:color 0, :x 8, :y 4}
                  {:color 0, :x 12, :y 4}
                  {:color 0, :x 16, :y 4}
                  {:color 0, :x 20, :y 4}
                  {:color 3, :x 24, :y 4}
                  {:color 0, :x 28, :y 4}

                  {:color 0, :x 0, :y 8}
                  {:color 3, :x 4, :y 8}
                  {:color 0, :x 8, :y 8}
                  {:color 0, :x 12, :y 8}
                  {:color 0, :x 16, :y 8}
                  {:color 0, :x 20, :y 8}
                  {:color 3, :x 24, :y 8}
                  {:color 0, :x 28, :y 8}

                  {:color 0, :x 0, :y 12}
                  {:color 3, :x 4, :y 12}
                  {:color 0, :x 8, :y 12}
                  {:color 0, :x 12, :y 12}
                  {:color 0, :x 16, :y 12}
                  {:color 0, :x 20, :y 12}
                  {:color 3, :x 24, :y 12}
                  {:color 0, :x 28, :y 12}

                  {:color 0, :x 0, :y 16}
                  {:color 3, :x 4, :y 16}
                  {:color 1, :x 8, :y 16}
                  {:color 3, :x 12, :y 16}
                  {:color 3, :x 16, :y 16}
                  {:color 3, :x 20, :y 16}
                  {:color 3, :x 24, :y 16}
                  {:color 0, :x 28, :y 16}

                  {:color 0, :x 0, :y 20}
                  {:color 1, :x 4, :y 20}
                  {:color 1, :x 8, :y 20}
                  {:color 1, :x 12, :y 20}
                  {:color 3, :x 16, :y 20}
                  {:color 1, :x 20, :y 20}
                  {:color 3, :x 24, :y 20}
                  {:color 0, :x 28, :y 20}

                  {:color 0, :x 0, :y 24}
                  {:color 3, :x 4, :y 24}
                  {:color 1, :x 8, :y 24}
                  {:color 3, :x 12, :y 24}
                  {:color 1, :x 16, :y 24}
                  {:color 3, :x 20, :y 24}
                  {:color 2, :x 24, :y 24}
                  {:color 0, :x 28, :y 24}

                  {:color 0, :x 0, :y 28}
                  {:color 2, :x 4, :y 28}
                  {:color 3, :x 8, :y 28}
                  {:color 3, :x 12, :y 28}
                  {:color 3, :x 16, :y 28}
                  {:color 2, :x 20, :y 28}
                  {:color 0, :x 24, :y 28}
                  {:color 0, :x 28, :y 28}])

(def T2 [{:color 3, :x 20, :y 0} {:color 3, :x 16, :y 0} {:color 3, :x 12, :y 0} {:color 3, :x 8, :y 0} {:color 3, :x 24, :y 4} {:color 3, :x 20, :y 4} {:color 3, :x 8, :y 4} {:color 3, :x 4, :y 4} {:color 3, :x 24, :y 8} {:color 3, :x 20, :y 8} {:color 3, :x 16, :y 8} {:color 3, :x 8, :y 8} {:color 3, :x 4, :y 8} {:color 3, :x 24, :y 12} {:color 3, :x 20, :y 12} {:color 3, :x 12, :y 12} {:color 3, :x 8, :y 12} {:color 3, :x 4, :y 12} {:color 3, :x 24, :y 16} {:color 3, :x 20, :y 16} {:color 3, :x 8, :y 16} {:color 3, :x 4, :y 16} {:color 3, :x 24, :y 20} {:color 3, :x 20, :y 20} {:color 3, :x 8, :y 20} {:color 3, :x 4, :y 20} {:color 3, :x 20, :y 24} {:color 3, :x 16, :y 24} {:color 3, :x 12, :y 24} {:color 3, :x 8, :y 24} {:color 3, :x 20, :y 0} {:color 3, :x 16, :y 0} {:color 3, :x 12, :y 0} {:color 3, :x 8, :y 0} {:color 3, :x 24, :y 4} {:color 3, :x 20, :y 4} {:color 3, :x 8, :y 4} {:color 3, :x 4, :y 4} {:color 3, :x 24, :y 8} {:color 3, :x 20, :y 8}])

(def SCREEN_WIDTH 800)

(def SCREEN_HEIGHT 600)

(def SCREEN_X 0)
(def SCREEN_Y 0)
(def SCALE 4)

(defn main-panel [f]
  (proxy [JPanel] []
    (java.awt.Component/paintComponent [^Graphics g]
      (proxy-super paintComponent g)
      (f g))))

(defn init-frame [{:keys [title width height x y draw-fn]}]
  (let [frame (JFrame. "Graphics2D Example")
        panel (proxy [JPanel] []
                (java.awt.Component/paintComponent [^Graphics g]
                  (proxy-super paintComponent g)
                  (draw-fn g)))]
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.add frame panel)
    (.setSize frame width height)
    (.setVisible frame true)
    (.setLocation frame x y)

    {:frame frame, :panel panel}))

(defn draw-dbg [ctx g]
  (println "ticks " (get-in @*ctx [:emu :ticks]))
  (let [all-px (ui/all-tile-px @*ctx SCALE 0x8000)
        color-px (filter #(pos? (:color %)) all-px)]
    (if (pos? (count color-px))
      (println (map #(str % "\n") all-px)))
    (doseq [px all-px]
      (.setColor g (get TILE_COLORS (:color px)))
      (.fillRect g (:x px) (:y px) SCALE SCALE))))

(defn draw [ctx g]
  (.setColor g (first (shuffle TILE_COLORS)))
  ;; (.clearRect g 0 0 SCREEN_WIDTH SCREEN_HEIGHT)
  (.fillRect g (rand 1000) (rand 1000) (rand 1000) (rand 1000)))

  ;; (let [g2d (cast Graphics2D (. g (getGraphics)))]
  ;;   (.setColor g2d Color/GREEN)
  ;;   (.clearRect g2d 0 0 SCREEN_WIDTH SCREEN_HEIGHT)
  ;;   (.fillRect g2d (rand 100) (rand 100) (rand 100) (rand 100))))
  ;;
(defn print-mem [ctx addr]
  (and ctx
       (println
        (format "0x%04X:%02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X"
          addr
          (bus/read! ctx (+ 0x0 addr))
          (bus/read! ctx (+ 0x1 addr))
          (bus/read! ctx (+ 0x2 addr))
          (bus/read! ctx (+ 0x3 addr))
          (bus/read! ctx (+ 0x4 addr))
          (bus/read! ctx (+ 0x5 addr))
          (bus/read! ctx (+ 0x6 addr))
          (bus/read! ctx (+ 0x7 addr))
          (bus/read! ctx (+ 0x8 addr))
          (bus/read! ctx (+ 0x9 addr))
          (bus/read! ctx (+ 0xA addr))
          (bus/read! ctx (+ 0xB addr))
          (bus/read! ctx (+ 0xC addr))
          (bus/read! ctx (+ 0xD addr))
          (bus/read! ctx (+ 0xE addr))
          (bus/read! ctx (+ 0xF addr))))))

(defn main-loop [ctx main dbg wait-ms]
  (while true
    (and wait-ms (Thread/sleep wait-ms))
    (print-mem @*ctx 0x8000)
    (print-mem @*ctx 0x8010)
    (print-mem @*ctx 0x8020)
    (print-mem @*ctx 0x8030)
    (print-mem @*ctx 0x8040)
    (print-mem @*ctx 0x8050)
    (print-mem @*ctx 0x8060)
    (print-mem @*ctx 0x8070)
    (print-mem @*ctx 0x8080)
    (print-mem @*ctx 0x8090)
    (print-mem @*ctx 0x80A0)
    (print-mem @*ctx 0x80B0)
    (print-mem @*ctx 0x80C0)
    (print-mem @*ctx 0x80D0)
    (print-mem @*ctx 0x80E0)
    (print-mem @*ctx 0x80F0)
    (print-mem @*ctx 0x8100)
    (print-mem @*ctx 0x8110)
    (print-mem @*ctx 0x8120)
    (print-mem @*ctx 0x8130)
    (print-mem @*ctx 0x8140)
    (print-mem @*ctx 0x8150)
    (print-mem @*ctx 0x8160)
    (print-mem @*ctx 0x8170)
    (print-mem @*ctx 0x8180)
    (print-mem @*ctx 0x8190)
    (print-mem @*ctx 0x81A0)
    (print-mem @*ctx 0x81B0)
    (print-mem @*ctx 0x81C0)
    (print-mem @*ctx 0x81D0)
    (print-mem @*ctx 0x81E0)
    (print-mem @*ctx 0x81F0)
    (println "")
    (print-mem @*ctx 0x8800)
    (print-mem @*ctx 0x8810)
    (print-mem @*ctx 0x8820)
    (print-mem @*ctx 0x8830)
    (print-mem @*ctx 0x8840)
    (println "")
    (print-mem @*ctx 0x9000)
    (print-mem @*ctx 0x9010)
    (print-mem @*ctx 0x9020)
    (print-mem @*ctx 0x9030)
    (print-mem @*ctx 0x9040)
    (. (:panel main) (repaint))
    (. (:panel dbg) (repaint))))
    ;; (draw ctx main-panel)
    ;; (draw-dbg ctx dbg-panel)))

(defn run [*ctx wait-ms]
  (let [main (init-frame {:draw-fn (fn [g] (draw *ctx g)), :width 800, :height 600, :x 0, :y 0})
        dbg-w (* 16 9 SCALE)
        dbg-h (* 32 64 SCALE)
        dbg   (init-frame {:draw-fn (fn [g] (draw-dbg *ctx g)), :width dbg-w, :height dbg-h, :x 810, :y 0})]
    (main-loop *ctx main dbg wait-ms)))

(defn -main []
  (run nil 1000))


(comment
  Color/GREEN

  (def main-frame (init-frame {:width 800, :height 600, :x 0, :y 0}))
  (def dbg-frame  (init-frame {:panel (dbg-panel) :width 200, :height 400, :x (+ 10 SCREEN_WIDTH), :y 0}))

  (.add main-frame (graphics-panel))
  (.add dbg-frame (graphics-panel))
  (.draw main-frame (graphics-panel))

  (.repaint gpanel)

  (graphics-panel)


  (def panel (proxy [JPanel] []
                    (paint [g] (draw @*ctx g))))

  (. panel (repaint))

  (. panel (getGraphics))
  (class panel)

  (first TILE_COLORS)

  ,,,)
