(ns gbemu.execution
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [clojure.pprint :refer [pprint]]
            [gbemu.stack :as stack]))

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
  (reduce (fn [ctx' [flag val]] (set-flag ctx' flag val)) ctx flags))

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

(defn- none [ctx]
  (throw (Exception. "No instruction! Fail")))

(defn- load [ctx]
  (let [{:keys [mode fetched-data cur-instr mem_dest dest_is_mem] :as cpu} (:cpu ctx)
        {:keys [reg1 reg2] :as inst} cur-instr]
        ;; _ (println cpu)]
    (cond
      (and dest_is_mem (r/sixteen-bit? reg2)) (bus/write-bus-16 ctx mem_dest fetched-data)
      dest_is_mem                            (bus/write-bus-16 ctx mem_dest fetched-data)
      (= mode :register_sp-plus-r8)          (let [hflag (< 0x10 (+ (bit-and 0x0F fetched-data)
                                                                    (bit-and 0x0F (r/read-reg ctx reg2))))
                                                   cflag (< 0x100 (+ (bit-and 0xFF fetched-data)
                                                                     (bit-and 0xFF (r/read-reg ctx reg2))))]
                                               (-> ctx
                                                   (set-flags {:h hflag :c cflag})
                                                   (r/write-reg reg1 (bit-and 0xFF (+ (r/read-reg ctx reg2) fetched-data)))))

      :else       (r/write-reg ctx reg1 fetched-data))))

(defn- load-high-ram [ctx]
  (let [{:keys [cur-instr fetched-data]} (:cpu ctx)
        {:keys [reg1 reg2]}              cur-instr
        ;; _ (println ctx)
        ctx'                             (if (= reg1 :a)
                                           (r/write-reg ctx reg1 (bus/read-bus (bit-or 0xFF00 fetched-data)))
                                           (bus/write-bus ctx (bit-or 0xFF00 fetched-data) (r/read-reg ctx reg2)))]
    (assoc-in ctx' [:cpu :emu-cycles] 1)))

(defn- di [ctx]
  (assoc-in ctx [:cpu :int-master-enabled] false))

(defn- xor [ctx]
  (let [a    (get-in ctx [:cpu :registers :a])
        data (get-in ctx [:cpu :fetched-data])
        res  (bit-xor a (bit-and data 0xFF))]
    (-> ctx
        (assoc-in [:cpu :registers :a] res)
        (set-flag :z (zero? res)))))

(defn- dec [ctx]
  (throw (Exception. "TODO IMPLEMENT DEC")))

(defn- pop [ctx]
  ;; TODO: emu-cycles b/t each 8bit stack pop
  (let [[val ctx'] (stack/pop-16 ctx)
        cur-instr (get-in ctx' [:cpu :cur-instr])
        reg1 (:reg1 cur-instr)]
    (if (= reg1 :af)
      (r/write-reg ctx' reg1 (bit-and 0xFFF0 val))
      (r/write-reg ctx' reg1 val))))

(defn- push [ctx]
  (let [cur-instr (get-in ctx [:cpu :cur-instr])
        reg1 (:reg1 cur-instr)
        _ (println "ctx before push" (:cpu ctx))
        ctx' (stack/push-16 ctx (r/read-reg ctx reg1))
        _ (println "ctx after push" (:cpu ctx'))]
    ctx'))

(defn- halt [ctx]
  (assoc-in ctx [:cpu :halted] true))

(def by-instruction
  {:none none
   :no-op identity
   :jump jump
   :di di
   :xor xor
   :dec dec
   :load load
   :pop pop
   :push push
   :halt halt
   :loadh load-high-ram})

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
