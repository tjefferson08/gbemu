(ns gbemu.system
  (:require [integrant.core :as ig]
            [clojure.tools.cli :refer [parse-opts]]
            [gbemu.cartridge :refer [load-cartridge]]
            [gbemu.cpu :as cpu]
            [gbemu.emu :as emu]))

(def config
  {::cartridge {:args (ig/ref ::cli-args)}
   ::cpu {}
   ::emu {:cpu (ig/ref ::cpu)}
   ::cli-args nil})

(def cli-options
  [["-r" "--rom ROM" "ROM path"]
   ["-h" "--help"]])

(defmethod ig/init-key ::cartridge [_ {:keys [args]}]
  (load-cartridge (get-in args [:options :rom])))

(defmethod ig/init-key ::cpu [_ _]
  (cpu/init))

(defmethod ig/init-key ::emu [_ _]
  (emu/init))

(defmethod ig/init-key ::cli-args [_ opts] opts)

(defn -main [& args]
  (let [opts (parse-opts args cli-options)
        cfg (assoc config ::cli-args opts)]
    (ig/init cfg)))
