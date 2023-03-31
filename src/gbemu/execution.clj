(ns gbemu.execution)

(defn- none [ctx]
  (throw (Exception. "No instruction! Fail")))

(defn- load [ctx]
  (println "TODO"))

(defn flag-set? [ctx flag]
  (let [flags (get-in ctx [:cpu :registers :f])]
    (case flag
      :z (pos? (bit-and 0x80 flags))
     false)))

(defn- set-flag [ctx flag val]
  (let [flags      (get-in ctx [:cpu :registers :f])
        bitmap     {:z 7, :n 6, :h 5, :c 4}
        bit        (or (bitmap flag) (throw (Exception. (str "Unknown flag " flag))))
        next-flags (bit-and 0xFF ((if val bit-set bit-clear) flags bit))]
    (assoc-in ctx [:cpu :registers :f] next-flags)))

(defn set-flags [ctx {:keys [z n h c] :as flags}]
  (reduce (fn [ctx' [flag val]] (set-flag ctx' flag val) ) ctx flags))

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

(defn- xor [ctx]
  (let [a    (get-in ctx [:cpu :registers :a])
        data (get-in ctx [:cpu :fetched-data])
        res  (bit-xor a (bit-and data 0xFF))]
    (-> ctx
      (assoc-in [:cpu :registers :a] res)
      (set-flag :z (zero? res)))))


(def by-instruction
  {:none none
   :no-op identity
   :jump jump
   :di di
   :xor xor
   :load load})

(defn execute [ctx]
  (let [inst (get-in ctx [:cpu :cur-instr])
        ;; _ (println inst)
        f (by-instruction (:type inst))]
        ;; _ (println f)]
    (if f
      (f ctx)
      (throw (Exception. (str "Unhandled instruction" inst))))))

(comment
  (format "%02X"
          (bit-and 0xFF (bit-set 0 7)))


  nil)
