(ns gbemu.execution.logic
  (:require [gbemu.execution.flags :as flags]
            [gbemu.cpu.registers :as r]
            [gbemu.bytes :as bytes]))

(defn xor [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [a    (r/read-reg ctx :a)
        res  (bit-xor a fetched-data)]
    (-> ctx
        (r/write-reg :a res)
        (flags/set-flags {:z (zero? res), :n false, :h false, :c false}))))

(defn and [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [a    (r/read-reg ctx :a)
        res  (bit-and a fetched-data)]
    (-> ctx
        (r/write-reg :a res)
        (flags/set-flags {:z (zero? res), :n false, :h true, :c false}))))

(defn or [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [a    (r/read-reg ctx :a)
        res  (bit-or a fetched-data)]
    (-> ctx
        (r/write-reg :a res)
        (flags/set-flags {:z (zero? res), :n false, :h false, :c false}))))

(defn cp [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [a    (r/read-reg ctx :a)
        res  (- a fetched-data)
        hd   (bytes/half-diff a res)]
    (-> ctx
        (flags/set-flags {:z (zero? res), :n true, :h (neg? hd), :c (neg? res)}))))

(defn rlca [ctx]
  (let [v   (r/read-reg ctx :a)
        c   (bit-test v 7)
        v'  (bit-and 0xFF (bit-shift-left v 1))
        v'' (if c (bit-set v' 0) v')]
    (-> ctx
        (r/write-reg :a v'')
        (flags/set-flags {:z false, :n false, :h false, :c c}))))


(defn rla [ctx]
  (let [v   (r/read-reg ctx :a)
        c   (flags/flag-set? ctx :c)
        c'  (bit-test v 7)
        v'  (bit-and 0xFF (bit-shift-left v 1))
        v'' (if c (bit-set v' 0) v')]
    (-> ctx
        (r/write-reg :a v'')
        (flags/set-flags {:z false, :n false, :h false, :c c'}))))


(defn rrca [ctx]
  (let [v   (r/read-reg ctx :a)
        c  (bit-test v 0)
        v'  (bit-and 0xFF (bit-shift-right v 1))
        v'' (if c (bit-set v' 7) v')]
    (-> ctx
        (r/write-reg :a v'')
        (flags/set-flags {:z false, :n false, :h false, :c c}))))

(defn rra [ctx]
  (let [v   (r/read-reg ctx :a)
        c   (flags/flag-set? ctx :c)
        c'  (bit-test v 7)
        v'  (bit-and 0xFF (bit-shift-right v 1))
        v'' (if c (bit-set v' 7) v')]
    (-> ctx
        (r/write-reg :a v'')
        (flags/set-flags {:z false, :n false, :h false, :c c'}))))
