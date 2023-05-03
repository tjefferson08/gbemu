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
            [gbemu.timer :as timer]))

(set! *warn-on-reflection* true)

(def cli-options
  [["-r" "--rom ROM" "ROM path"]
   [nil "--tick-limit TICK_LIMIT" "exit after excuting specified number of ticks"
    :default nil
    :parse-fn #(Integer/parseInt %)]
   [nil "--headless" "Set to true for headless mode (false by default)"]
   ["-h" "--help"]])

(defn init [rom]
 {:emu (emu/init)
  :cartridge (load-cartridge rom)
  :io (io/init)
  :debug (debug/init)
  :cpu (cpu/init)
  :timer (timer/init)
  :ram (ram/init)})

(defn -main [& args]
  (let [opts (parse-opts args cli-options)
        {:keys [headless rom tick-limit]} (:options opts)
        ctx (-> (init rom)
                (update :emu assoc :headless headless :tick-limit tick-limit))]
    (with-open [o (jio/writer "test.txt")]
      (emu/run (assoc ctx :log o)))))
