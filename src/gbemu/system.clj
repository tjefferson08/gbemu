(ns gbemu.system
  (:require [integrant.core :as ig]
            [clojure.tools.cli :refer [parse-opts]]
            [gbemu.cartridge :refer [load-cartridge]]
            [gbemu.cpu.core :as cpu]
            [gbemu.emu :as emu]
            [gbemu.ram :as ram]
            [gbemu.io :as io]
            [clojure.java.io :as jio]
            [gbemu.debug :as debug]
            [gbemu.timer :as timer]
            [gbemu.ppu :as ppu]
            [gbemu.lcd :as lcd]
            [gbemu.dma.state :as dma]))

(set! *warn-on-reflection* true)

(def cli-options
  [["-r" "--rom ROM" "ROM path"]
   ["-d" "--debug-log DEBUG_LOG" "DEBUG_LOG filepath"]
   [nil "--tick-limit TICK_LIMIT" "exit after excuting specified number of ticks"
    :default nil
    :parse-fn #(Integer/parseInt %)]
   [nil "--headless" "Set to true for headless mode (false by default)"]
   ["-h" "--help"]])

(defn init [rom]
  (merge (lcd/init)
         (dma/init)
         {:emu (emu/init)
          :cartridge (load-cartridge rom)
          :io (io/init)
          :debug (debug/init)
          :cpu (cpu/init)
          :ppu (ppu/init)
          :timer (timer/init)
          :ram (ram/init)}))


(defn -main [& args]
  (let [opts (parse-opts args cli-options)
        {:keys [headless rom tick-limit debug-log]} (:options opts)
        ctx (-> (init rom)
                (update :emu assoc :headless headless :tick-limit tick-limit))]
    (if debug-log
      (with-open [o (jio/writer debug-log)]
        (emu/run (assoc ctx :log (fn log [s] (.write o s))))
        (.flush o))
      (emu/run (assoc ctx :log (fn noop [s]))))))
