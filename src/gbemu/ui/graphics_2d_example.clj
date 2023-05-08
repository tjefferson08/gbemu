(ns gbemu.ui.graphics-2d-example
  (:import [java.awt Color Graphics Graphics2D Dimension]
           [java.awt.image BufferedImage]
           [javax.swing JFrame JPanel])
  (:require [gbemu.log :as log]
            [gbemu.state :refer [*ctx]]
            [gbemu.bus :as bus]))

; embedded nREPL
(require '[nrepl.server :refer [start-server stop-server]])
(defonce server (start-server :port 7889))

;; static unsigned long tile_colors[4] = {0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000};
(def TILE_COLORS [(new Color 0xFF 0xFF 0xFF 0xFF) (new Color 0xFF 0xAA 0xAA 0xAA)
                  (new Color 0xFF 0x55 0x55 0x55) (new Color 0xFF 0 0 0)])

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
    ;; (println "frame " frame " panel " panel)
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.add frame panel)
    (.setSize frame width height)
    (.setVisible frame true)
    (.setLocation frame x y)

    {:frame frame, :panel panel}))

(defn draw-dbg [ctx g]
  (.setColor g Color/BLUE)
  ;; (.clearRect g 0 0 SCREEN_WIDTH SCREEN_HEIGHT)
  (.fillRect g (rand 100) (rand 100) (rand 100) (rand 100)))

(defn draw [ctx g]
  (.setColor g Color/GREEN)
  ;; (.clearRect g 0 0 SCREEN_WIDTH SCREEN_HEIGHT)
  (.fillRect g (rand 100) (rand 100) (rand 100) (rand 100)))

  ;; (let [g2d (cast Graphics2D (. g (getGraphics)))]
  ;;   (.setColor g2d Color/GREEN)
  ;;   (.clearRect g2d 0 0 SCREEN_WIDTH SCREEN_HEIGHT)
  ;;   (.fillRect g2d (rand 100) (rand 100) (rand 100) (rand 100))))

(defn main-loop [ctx main dbg wait-ms]
  (while true
    (and wait-ms (Thread/sleep wait-ms))
    (. (:panel main) (repaint))
    (. (:panel dbg) (repaint))))
    ;; (draw ctx main-panel)
    ;; (draw-dbg ctx dbg-panel)))

(defn run [ctx wait-ms]
  (let [main (init-frame {:draw-fn (fn [g] (draw ctx g)), :width 800, :height 600, :x 0, :y 0})
        dbg  (init-frame {:draw-fn (fn [g] (draw-dbg ctx g)), :width 200, :height 400, :x 810, :y 0})]
    (main-loop ctx main dbg wait-ms)))

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
