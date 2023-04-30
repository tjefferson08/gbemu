(ns gbemu.log)

(defn stderr [msg]
  (.println *err* msg))
