(ns gbemu.execution.logic
  (:require [gbemu.execution.flags :as flags]
            [gbemu.cpu.registers :as r]
            [gbemu.bytes :as bytes]))

(defn xor [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [a    (r/read-reg ctx :a)
        res  (bit-xor a fetched-data)]
    (-> ctx
        (r/write-reg :a res)
        (flags/set-flags {:z (zero? res)}))))

(defn and [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [a    (r/read-reg ctx :a)
        res  (bit-and a fetched-data)]
    (-> ctx
        (r/write-reg :a res)
        (flags/set-flags {:z (zero? res), :n 0, :h 1, :c 0}))))

(defn or [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [a    (r/read-reg ctx :a)
        res  (bit-or a fetched-data)]
    (-> ctx
        (r/write-reg :a res)
        (flags/set-flags {:z (zero? res), :n 0, :h 0, :c 0}))))

(defn cp [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [a    (r/read-reg ctx :a)
        res  (- a fetched-data)
        hd   (bytes/half-diff a res)]
    (-> ctx
        (flags/set-flags {:z (zero? res), :n 1, :h (neg? hd), :c (neg? res)}))))
