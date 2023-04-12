(ns gbemu.execution.jump
  (:require [gbemu.execution.flags :as flags]))

(defn- check-cond [ctx]
  (let [z-set? (flags/flag-set? ctx :z)
        c-set? (flags/flag-set? ctx :c)
        inst (get-in ctx [:cpu :cur-instr])]
    (case (:cond inst)
      :c c-set?
      :nc (not c-set?)
      :z z-set?
      :nz (not z-set?)
      true)))

(defn jump [ctx]
  (if (check-cond ctx)
    (let [new-pc (get-in ctx [:cpu :fetched-data])]
      ;; emu/cycles 1())
      (assoc-in ctx [:cpu :registers :pc] new-pc))))
