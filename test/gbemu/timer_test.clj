(ns gbemu.timer-test
  (:require [gbemu.timer :as sut]
            [clojure.test :refer :all]
            [gbemu.cpu.core :as cpu]
            [gbemu.cpu.registers :as r]
            [gbemu.cpu.interrupt :as interrupt]))

(defn init []
  {:timer (sut/init)
   :cpu   (cpu/init)})


(deftest div-ticks-while-disabled
  (let [ctx                   (init)
        ctx-after-single-tick (sut/tick ctx)
        ctx-after-5-ticks     (last (take 6 (iterate sut/tick ctx)))]

    (is (not (sut/enabled? ctx)))
    (is (= 0xAC00 (get-in ctx [:timer :div])))

    (is (not (sut/enabled? ctx-after-single-tick)))
    (is (= 0xAC01 (get-in ctx-after-single-tick [:timer :div])))

    (is (not (sut/enabled? ctx-after-5-ticks)))
    (is (= 0xAC05 (get-in ctx-after-5-ticks [:timer :div])))))

(deftest interrupt-after-32-ticks
  (let [ctx         (-> (init)
                        (r/write-ie-reg 0x04)
                        (sut/write 0xFF04 0x99) ;; start DIV at 0
                        (sut/write 0xFF05 0xFE) ;; interrupt every two TIMA ticks
                        (sut/write 0xFF06 0xFE) ;; start TIMA at 0xFE, two cycles from an interrupt
                        (sut/write 0xFF07 0x05))    ;; Clock = enabled, CPU / 16 (2r10)
        ticks       (take 33 (iterate sut/tick ctx))
        ctx-tick-15 (nth ticks 15)
        ctx-tick-16 (nth ticks 16)
        ctx-tick-31 (nth ticks 31)
        ctx-tick-32 (nth ticks 32)]

    (is (sut/enabled? ctx))
    (is (= {:div 0, :tac 0x05, :tma 0xFE, :tima 0xFE} (:timer ctx)))

    (is (= {:div 0x0F, :tac 0x05, :tma 0xFE, :tima 0xFE} (:timer ctx-tick-15)))
    (is (not (interrupt/ready? ctx-tick-15 :timer)))

    (is (= {:div 0x10, :tac 0x05, :tma 0xFE, :tima 0xFF} (:timer ctx-tick-16)))
    (is (not (interrupt/ready? ctx-tick-16 :timer)))

    (is (= {:div 0x1F, :tac 0x5, :tma 0xFE, :tima 0xFF} (:timer ctx-tick-31)))
    (is (not (interrupt/ready? ctx-tick-31 :timer)))

    (is (= {:div 0x20, :tac 0x5, :tma 0xFE, :tima 0xFE} (:timer ctx-tick-32)))
    (is (interrupt/ready? ctx-tick-32 :timer))))


(comment
  (take 10 (iterate inc 10))


 ,,,)
