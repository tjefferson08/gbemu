(ns gbemu.execution.jump
  (:require [gbemu.execution.flags :as flags]
            [gbemu.stack :as stack]
            [gbemu.cpu.registers :as r]
            [gbemu.bytes :as bytes]))

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

(defn- jump*
  ([ctx addr] (jump* ctx addr false))
  ([ctx addr push-pc]
   (if (check-cond ctx)
     (let [pc (r/read-reg ctx :pc)
           ctx' (if push-pc (stack/push-16 ctx pc) ctx) ;; TODO 2 more cycles with a push
           new-pc addr]
       ;; emu/cycles 1())
       (r/write-reg ctx' :pc new-pc)))))


(defn jump [ctx]
  (jump* ctx (get-in ctx [:cpu :fetched-data])))

(defn call [ctx]
  (jump* ctx (get-in ctx [:cpu :fetched-data]) true))

(defn jump-rel [ctx]
  (let [offset (get-in ctx [:cpu :fetched-data])]
    (jump* ctx (+ (r/read-reg ctx :pc) offset) false)))

(defn ret [ctx]
  ;; emu cycles 1 if cond not= none)
  (when (check-cond ctx)
    (let [[lo ctx'] (stack/pop ctx)
          ;; _ (emu-cycles 1)
          [hi ctx''] (stack/pop ctx')
          new-pc     (bytes/->d16 lo hi)]
          ;; _ (println "ret ctx new-pc" new-pc (:cpu ctx''))]
      (r/write-reg ctx'' :pc new-pc))))
      ;; cycles 1 again

(defn ret-i [ctx]
  (-> ctx
      ret
      assoc-in [:cpu :int-master-enabled] true))

(defn rst [ctx]
  (jump* ctx (get-in ctx [:cpu :cur-instr :param]) true))
