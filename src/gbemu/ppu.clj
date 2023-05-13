(ns gbemu.ppu
  (:require [gbemu.lcd :as lcd]
            [gbemu.cpu.interrupt :as interrupt]))

(def LINES_PER_FRAME 154)
(def TICKS_PER_LINE 456)
(def YRES 144)
(def XRES 160)

(defn init []
  {:oam-ram (apply vector-of :byte (repeat 160 0))
   :vram    (apply vector-of :byte (repeat 0x2000 0))
   :current-frame 0, :line-ticks 0,
   :video-buffer (apply vector-of :int (repeat (* XRES YRES) 0))})

(defn y-flip? [])
(defn x-flip? [])
(defn palette-num [])

(defn oam-tick [ctx]
  ;; (println "oam-tick " (get-in ctx [:ppu :line-ticks]))
  (if (<= 80 (get-in ctx [:ppu :line-ticks]))
     (lcd/lcds-set-mode ctx :xfer)
     ctx))

(defn xfer-tick [ctx]
  ;; (println "xfer-tick " (get-in ctx [:ppu :line-ticks]))
  (if (<= (+ 80 172) (get-in ctx [:ppu :line-ticks]))
    (lcd/lcds-set-mode ctx :hblank)
    ctx))

(defn increment-ly [ctx]
  (let [ly'  (inc (lcd/ly ctx))
        ctx' (assoc-in ctx [:lcd :ly] ly')
        lyc? (= ly' (lcd/ly-compare ctx'))]
    (cond
      (and lyc? (lcd/lcds-stat-int? ctx')) (-> ctx' (lcd/lcds-set-lyc true) (interrupt/request :lcd-stat))
      lyc?                                 (-> ctx' (lcd/lcds-set-lyc true))
      :else                                (-> ctx' (lcd/lcds-set-lyc false)))))

(defn vblank-tick [ctx]
  ;; (println "vblank-tick " (get-in ctx [:ppu :line-ticks]) " ly " (lcd/ly ctx))
  (if (> TICKS_PER_LINE (get-in ctx [:ppu :line-ticks]))
    ctx
    (let [ctx'  (increment-ly ctx)
          ctx'' (if (<= LINES_PER_FRAME (lcd/ly ctx'))
                   (-> ctx' (lcd/lcds-set-mode :oam) (assoc-in [:lcd :ly] 0))
                   ctx')]
      (assoc-in ctx'' [:ppu :line-ticks] 0))))

(defn hblank-tick [ctx]
  ;; (println "hblank-tick " (get-in ctx [:ppu :line-ticks]) " ly " (lcd/ly ctx))
  (let [handle-y-ovf (fn [c] (-> c
                               (lcd/lcds-set-mode :vblank)
                               (interrupt/request :vblank)
                               (#(if (lcd/lcds-stat-int? % :vblank) (interrupt/request % :lcd-stat) %))
                               (update-in [:ppu :current-frame] inc)))]
                                 ;; TODO calculate FPS

   (if (> TICKS_PER_LINE (get-in ctx [:ppu :line-ticks]))
     ctx
     (-> (increment-ly ctx)
         (#(if (<= YRES (lcd/ly %))
             (handle-y-ovf %)
             (lcd/lcds-set-mode % :oam)))
         (assoc-in [:ppu :line-ticks] 0)))))

(def TICK_DISPATCH
  {:oam oam-tick
   :xfer xfer-tick
   :vblank vblank-tick
   :hblank hblank-tick})

(defn tick [ctx]
  ;; (println "tick " (get-in ctx [:ppu :line-ticks]) " mode " (lcd/lcds-mode ctx))
  (let [mode (lcd/lcds-mode ctx)
        f (TICK_DISPATCH mode)]
     (-> ctx (update-in [:ppu :line-ticks] inc)
             f)))

(defn read-oam [ctx addr]
  (let [address (if (<= 0xFE00 addr) (- addr 0xFE00) addr)]
    (get-in ctx [:ppu :oam-ram address])))

(defn write-oam [ctx addr value]
  (let [address (if (<= 0xFE00 addr) (- addr 0xFE00) addr)]
    (assoc-in ctx [:ppu :oam-ram address] (unchecked-byte value))))

(defn read-vram [ctx addr]
  (let [address (- addr 0x8000)]
    (get-in ctx [:ppu :vram address])))

(defn write-vram [ctx addr value]
  ;; (println "writing vram " addr ", " value)
  (let [address (- addr 0x8000)]
    (assoc-in ctx [:ppu :vram address] (unchecked-byte value))))
