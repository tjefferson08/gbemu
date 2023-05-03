(ns gbemu.stack
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]))

(defn push [ctx val]
  (let [sp (r/read-reg ctx :sp)
        sp' (bit-and 0xFFFF (dec sp))
        ;; _   (println "sp'" sp')
        ctx' (bus/write-bus ctx sp' val)
        ;; _    (println "ctx after bus write" (:cpu ctx'))
        ctx'' (r/write-reg ctx' :sp sp')]
        ;; _    (println "ctx after reg write" (:cpu ctx''))]
     ctx''))

(defn push-16 [ctx val]
  (let [ctx'  (push ctx  (bit-and 0xFF (bit-shift-right val 8)))
        ctx'' (push ctx' (bit-and 0xFF val))]
    ctx''))

(defn pop [ctx]
  (let [sp (r/read-reg ctx :sp)
        sp' (inc sp)
        [val ctx'] (bus/read ctx sp)
        ctx''      (r/write-reg ctx' :sp sp')]
     [val ctx'']))


(defn pop-16 [ctx]
  (let [[lo ctx'] (pop ctx)
        [hi ctx''] (pop ctx')
        val (bit-or lo (bit-shift-left hi 8))]
   [val ctx'']))

(comment
  (format "%04X" (bit-shift-left 0x04 1))

  (format "%04X" (bit-and 0xFF00 (bit-shift-left 8 0xCC)))

  (format "%06X" (bit-and 0xFF (bit-shift-right 0xBBCC 8)))

  (format "%06X" (bit-and 0xFF 0xBBCC))

  nil)
