(ns gbemu.execution.flags
  (:require [gbemu.cpu.registers :as r]))

(defn- bit-for [flag]
  (flag {:z 7, :n 6, :h 5, :c 4}))

(defn flag-set? [ctx flag]
  (let [flags (if (map? ctx) (r/read-reg ctx :f) ctx)]
    (bit-test flags (bit-for flag))))

(defn- set-flag [ctx flag val]
  (let [flags      (get-in ctx [:cpu :registers :f])
        bit        (or (bit-for flag) (throw (Exception. (str "Unknown flag " flag))))
        next-flags (bit-and 0xFF ((if val bit-set bit-clear) flags bit))]
    (assoc-in ctx [:cpu :registers :f] next-flags)))

(defn set-flags [ctx {:keys [z n h c] :as flags}]
  (reduce (fn [ctx' [flag val]] (set-flag ctx' flag val)) ctx flags))
