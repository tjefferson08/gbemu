(ns gbemu.cpu.fetch
  (:require [gbemu.bus :as bus]
            [gbemu.cpu.registers :as r]))

(def defaults {:mem_dest 0 :dest_is_mem false})

(defn- reg1 [ctx] (get-in ctx [:cpu :cur-instr :reg1]))
(defn- reg2 [ctx] (get-in ctx [:cpu :cur-instr :reg2]))
(defn- read-reg1 [ctx] (r/read-reg ctx (reg1 ctx)))
(defn- read-reg2 [ctx] (r/read-reg ctx (reg2 ctx)))

(defmulti fetch-data* (fn [ctx] (get-in ctx [:cpu :cur-instr :mode])))

(defmethod fetch-data* :a8_register [ctx]
 (let [pc          (r/read-reg ctx :pc)
       [addr ctx'] (bus/read ctx pc 4)
       changes     {:mem_dest (bit-or 0xFF00 addr), :dest_is_mem true}]
   (-> ctx' (update :cpu merge defaults changes)
            (r/write-reg :pc (inc pc)))))

(defmethod fetch-data* :implied [ctx] ctx)

(defmethod fetch-data* :memloc [ctx]
  (let [addr (r/read-reg ctx (reg1 ctx))
        [data ctx'] (bus/read ctx addr 4)]
    (update ctx' :cpu merge defaults {:fetched-data data, :mem_dest addr, :dest_is_mem true})))

(defmethod fetch-data* :memloc_d8 [ctx]
  (let [[pc, addr] (r/read-regs ctx :pc (reg1 ctx))
        pc'       (inc pc)
        [d8 ctx'] (bus/read ctx pc)]
   (-> ctx' (update :cpu merge defaults {:mem_dest addr, :fetched-data d8, :dest_is_mem true})
            (r/write-reg :pc (inc pc)))))

(defmethod fetch-data* :register [ctx]
  (update ctx :cpu merge defaults {:fetched-data (read-reg1 ctx)}))

(defmethod fetch-data* :register_register [ctx]
  (update ctx :cpu merge defaults {:fetched-data (read-reg2 ctx)}))

(defmethod fetch-data* :memloc_register [ctx]
  (let [[addr, data] (r/read-regs ctx (reg1 ctx) (reg2 ctx))
        addr'        (if (= :c (reg1 ctx)) (bit-or 0xFF00 addr) addr)
        changes      {:mem_dest addr', :dest_is_mem true, :fetched-data data}]
    (update ctx :cpu merge defaults changes)))

(defmethod fetch-data* :register_memloc [ctx]
  (let [addr         (read-reg2 ctx)
        addr'        (if (= :c (reg2 ctx)) (bit-or 0xFF00 addr) addr)
        [data ctx']  (bus/read ctx addr' 4)]
    (update ctx' :cpu merge defaults {:fetched-data data})))

(defn- d8-fetch [ctx]
  (let [pc (r/read-reg ctx :pc)
        pc' (inc pc)
        [d8 ctx'] (bus/read ctx pc)]
    (-> ctx' (update :cpu merge defaults {:fetched-data d8})
             (r/write-reg :pc pc'))))

(defmethod fetch-data* :d8 [ctx] (d8-fetch ctx))
(defmethod fetch-data* :register_a8 [ctx] (d8-fetch ctx))
(defmethod fetch-data* :register_d8 [ctx] (d8-fetch ctx))

(defmethod fetch-data* :register_memloc+ [ctx]
  (let [addr (read-reg2 ctx)
        [data, ctx'] (bus/read ctx (read-reg2 ctx) 4)]
    (-> ctx' (update :cpu merge defaults {:fetched-data data})
             (r/write-reg (reg2 ctx) (inc addr)))))

(defmethod fetch-data* :register_memloc- [ctx]
  (let [addr (read-reg2 ctx)
        [data, ctx'] (bus/read ctx (read-reg2 ctx) 4)]
    (-> ctx' (update :cpu merge defaults {:fetched-data data})
             (r/write-reg (reg2 ctx) (dec addr)))))

(defmethod fetch-data* :memloc+_register [ctx]
  (let [[addr, data] (r/read-regs ctx (reg1 ctx) (reg2 ctx))]
    (-> ctx (update :cpu merge defaults {:fetched-data data, :mem_dest addr, :dest_is_mem true})
            (r/write-reg (reg1 ctx) (inc addr)))))

(defmethod fetch-data* :memloc-_register [ctx]
  (let [[addr, data] (r/read-regs ctx (reg1 ctx) (reg2 ctx))]
    (-> ctx (update :cpu merge defaults {:fetched-data data, :mem_dest addr, :dest_is_mem true})
            (r/write-reg (reg1 ctx) (dec addr)))))

(defmethod fetch-data* :register_sp-plus-r8 [ctx]
  (let [pc  (r/read-reg ctx :pc)
        pc' (inc pc)
        [d ctx'] (bus/read ctx pc 4)]
    (-> ctx' (update :cpu merge defaults {:fetched-data d})
             (r/write-reg :pc pc'))))

(defn- d16-fetch* [ctx]
  (let [pc  (r/read-reg ctx :pc)
        [lo ctx'] (bus/read ctx pc 4)
        [hi ctx''] (bus/read ctx' (inc pc) 4)
        d16 (bit-or (bit-and 0x00FF lo) (bit-and 0xFF00 (bit-shift-left hi 8)))]
    [d16 (+ 2 pc) ctx'']))

(defn- d16-fetch [ctx]
  (let [[d16 pc' ctx'] (d16-fetch* ctx)]
    (-> ctx' (update :cpu merge defaults {:fetched-data d16})
             (r/write-reg :pc pc'))))

(defmethod fetch-data* :d16 [ctx] (d16-fetch ctx))
(defmethod fetch-data* :register_d16 [ctx] (d16-fetch ctx))

(defmethod fetch-data* :d16_register [ctx]
  (let [[addr pc' ctx'] (d16-fetch* ctx)]
    (-> ctx' (update :cpu merge defaults {:fetched-data (read-reg2 ctx'), :mem_dest addr, :dest_is_mem true})
             (r/write-reg :pc pc'))))

(defmethod fetch-data* :register_a16 [ctx]
  (let [[addr pc' ctx'] (d16-fetch* ctx)
        [data ctx'']    (bus/read ctx addr 4)]
    (-> ctx'' (update :cpu merge defaults {:fetched-data data})
              (r/write-reg :pc pc'))))

(defn fetch-data [ctx]
  (fetch-data* ctx))

(comment

 (format "%04X" (bit-or -1 (bit-shift-left (bit-and 0x00ff -33) 8)))


 (unchecked-byte -1)

 (bit-shift-left 0xDF 8)
 (bit-or (bit-and 0x00FF -1) (bit-and 0xFF00 (bit-shift-left -33 8)))

 (bit-shift-left -33 8)

 (bit-or 0xFF 0xDF00)

 0xDFFF


 nil)
