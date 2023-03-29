(ns gbemu.system
  (:require [integrant.core :as ig]
            [clojure.tools.cli :refer [parse-opts]]
            [gbemu.cartridge :refer [load-cartridge]]
            [gbemu.cpu :as cpu]
            [gbemu.emu :as emu]))

(def cli-options
  [["-r" "--rom ROM" "ROM path"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [opts (parse-opts args cli-options)]
    (emu/run {:emu {:paused false, :running true, :ticks 0}
              :cartridge (load-cartridge (get-in opts [:options :rom]))
              :cpu (cpu/init)})))
