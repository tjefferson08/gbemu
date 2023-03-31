(ns gbemu.execution)

(defn- none [ctx]
  (throw (Exception. "No instruction! Fail")))

(defn- load [ctx]
  (println "TODO"))

(defn flag-set? [ctx flag]
  (let [flags (get-in ctx [:cpu :registers :f])]
    (case flag
      :z (pos? (bit-and 0x01 flags))
     false)))

(defn- check-cond [ctx]
  (let [z-set? (flag-set? ctx :z)
        c-set? (flag-set? ctx :c)
        inst (get-in ctx [:cpu :cur-instr])]
    (case (:cond inst)
        :c c-set?
        :nc (not c-set?)
        :z z-set?
        :nz (not z-set?)
        true)))


(defn- jump [ctx]
  (if (check-cond ctx)
    (let [new-pc (get-in ctx [:cpu :fetched-data])]
      ;; emu/cycles 1())
      (assoc-in ctx [:cpu :registers :pc] new-pc))))

(defn- di [ctx]
  (assoc-in ctx [:cpu :int-master-enabled] false))

(def by-instruction
  {:none none
   :no-op identity
   :jump jump
   :di di
   :load load})

(defn execute [ctx]
  (let [inst (get-in ctx [:cpu :cur-instr])
        ;; _ (println inst)
        f (by-instruction (:type inst))]
        ;; _ (println f)]
    (if f
      (f ctx)
      (throw (Exception. (str "Unhandled instruction" inst))))))
