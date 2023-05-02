(ns gbemu.timer
  (:require [gbemu.cpu.interrupt :as interrupt]))

(defn init []
   {:div 0xAC00,
    :tac 0
    :tma 0
    :tima 0})

(defn tick [ctx]
  (let [{{:keys [div tac tima tma]} :timer} ctx
        div'                 (inc div)
        enabled?             (bit-test tac 2)
        clock                (bit-and 2r0011 tac)
        bit                  ({2r00 9, 2r01 3, 2r10 5, 2r11 7} clock)
        tick?                (and enabled? (bit-test div bit) (not (bit-test div' bit)))
        tima'                (if tick? (inc tima) tima)
        [tima'', interrupt]  (if (= 0xFF tima') [tma true] [tima' false])
        ctx'                 (assoc-in ctx :div div'
                                           :tima tima'')]
     (if interrupt
       (interrupt/request ctx :timer)
       ctx)))

(defn write [ctx addr value]
  (case addr
    0xFF04 (assoc-in ctx [:timer :div] 0)
    0xFF05 (assoc-in ctx [:timer :tima] value)
    0xFF06 (assoc-in ctx [:timer :tma] value)
    0xFF07 (assoc-in ctx [:timer :tac] value)))

(defn read [ctx addr]
  (case addr
    0xFF04 (bit-shift-right (get-in ctx [:timer :div]) 8)
    0xFF05 (get-in ctx [:timer :tima])
    0xFF06 (get-in ctx [:timer :tma])
    0xFF07 (get-in ctx [:timer :tac])))
