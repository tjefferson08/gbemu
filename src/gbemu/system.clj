(ns gbemu.system
  (:require [integrant.core :as ig]
            [clojure.tools.cli :refer [parse-opts]]
            [gbemu.cartridge :refer [load-cartridge]]
            [gbemu.cpu.core :as cpu]
            [gbemu.emu :as emu]
            [gbemu.ram :as ram]))

(def cli-options
  [["-r" "--rom ROM" "ROM path"]
   ["-h" "--help"]])

(defn init [rom]
 {:emu (emu/init)
  :cartridge (load-cartridge rom)
  :cpu (cpu/init)
  :ram (ram/init)})

(defn boot [rom]
  (emu/run (init rom)))

(defn -main [& args]
  (let [opts (parse-opts args cli-options)]
    (boot (get-in opts [:options :rom]))))
