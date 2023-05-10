(ns gbemu.ui.graphics-2d-example
  (:import [java.awt Color Graphics Graphics2D Dimension]
           [java.awt.image BufferedImage]
           [javax.swing JFrame JPanel])
  (:require [gbemu.log :as log]
            [gbemu.state :refer [*ctx]]
            [gbemu.ui.core :as ui]
            [gbemu.bus :as bus]))

;; TODO embedded nREPL
;; (require '[nrepl.server :refer [start-server stop-server]])
;; (defonce server (start-server :port 7889))

;; static unsigned long tile_colors[4] = {0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000};
(def TILE_COLORS [(new Color 0xFFFFFF) (new Color 0xAAAAAA)
                  (new Color 0x555555) (new Color 0)])

(def SCREEN_WIDTH 800)
(def SCREEN_HEIGHT 600)

(def SCREEN_X 0)
(def SCREEN_Y 0)
(def SCALE 4)

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
  (let [all-px (ui/all-tile-px @*ctx SCALE 0x8000)]
    (doseq [px all-px]
      (.setColor g (get TILE_COLORS (:color px)))
      (.fillRect g (:x px) (:y px) SCALE SCALE))))

(defn draw [ctx g]
  (.setColor g (first (shuffle TILE_COLORS)))
  (.fillRect g (rand 1000) (rand 1000) (rand 1000) (rand 1000)))

(defn main-loop [ctx main dbg wait-ms]
  (while true
    (and wait-ms (Thread/sleep wait-ms))
    (. (:panel main) (repaint))
    (. (:panel dbg) (repaint))))

(defn run [*ctx wait-ms]
  (let [main (init-frame {:draw-fn (fn [g] (draw *ctx g)), :width 800, :height 600, :x 0, :y 0})
        dbg-w (* 16 9 SCALE)
        dbg-h (* 32 64 SCALE)
        dbg   (init-frame {:draw-fn (fn [g] (draw-dbg *ctx g)), :width dbg-w, :height dbg-h, :x 810, :y 0})]
    (main-loop *ctx main dbg wait-ms)))

(defn -main []
  (run nil 1000))

(comment

  ,,,)
