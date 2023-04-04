(ns gbemu.stack
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]))

(defn push [ctx val]
  (let [sp (r/read-reg ctx :sp)
        sp' (dec sp)]
    (-> ctx
        (bus/write-bus sp' val)
        (r/write-reg :sp sp'))))

(defn push-16 [ctx val]
  (-> ctx (push (bit-and 0xFF00 (bit-shift-left 8 val)))
          (push (bit-and 0x00FF val))))

(defn pop [ctx]
  (let [sp (r/read-reg ctx :sp)
        sp' (inc sp)
        val (bus/bus-read ctx)
        ctx' (-> ctx (r/write-reg :sp sp'))]
     [val ctx']))


(defn pop-16 [ctx]
  (let [[ctx' lo] (pop ctx)
        [ctx'' hi] (pop ctx')
        val (bit-or lo (bit-shift-left 8 hi))]
   [val ctx']))
