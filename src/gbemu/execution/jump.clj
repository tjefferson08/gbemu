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
   (let [pc (r/read-reg ctx :pc)]
     (if (check-cond ctx)
       (let [ctx' (if push-pc (stack/push-16 ctx pc) ctx) ;; TODO 2 more cycles with a push
             new-pc addr]
         ;; emu/cycles 1())
         (r/write-reg ctx' :pc new-pc))
       ctx))))


(defn jump [ctx]
  (jump* ctx (get-in ctx [:cpu :fetched-data])))

(defn call [ctx]
  (jump* ctx (get-in ctx [:cpu :fetched-data]) true))

(defn jump-rel [ctx]
  (let [offset (get-in ctx [:cpu :fetched-data])
        new-pc (bytes/to-u16 (+ (r/read-reg ctx :pc) (bytes/extend-sign offset)))
        ;; _ (println "ctx" (:cpu ctx))
        ctx'  (jump* ctx new-pc false)]
        ;; _ (println "ctx'" (:cpu ctx'))]
    ctx'))

(defn ret [ctx]
  ;; emu cycles 1 if cond not= none)
  (if (check-cond ctx)
    (let [[lo ctx'] (stack/pop ctx)
          ;; _ (emu-cycles 1)
          [hi ctx''] (stack/pop ctx')
          new-pc     (bytes/->d16 lo hi)]
      (r/write-reg ctx'' :pc new-pc))
    ctx))
      ;; cycles 1 again

(defn ret-i [ctx]
  (-> ctx
      (assoc-in [:cpu :int-master-enabled] true)
      ret))

(defn rst [ctx]
  (jump* ctx (get-in ctx [:cpu :cur-instr :param]) true))
