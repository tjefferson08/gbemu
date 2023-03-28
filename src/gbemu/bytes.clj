(ns gbemu.bytes)

(defn hexify "Convert byte sequence to hex string" [coll]
  (let [hex [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \A \B \C \D \E \F]]
      (letfn [(hexify-byte [b]
               (let [v (bit-and b 0xFF)]
                 [(hex (bit-shift-right v 4)) (hex (bit-and v 0x0F))]))]
        (apply str (mapcat hexify-byte coll)))))

(defn hexify-str [s]
  (hexify (.getBytes s)))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [in (clojure.java.io/input-stream x)
              out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy in out)
    (.toByteArray out)))

(defn slice
  "Take a slice (copy) array of byte array `bytes`"
  [bytes from to]
  (java.util.Arrays/copyOfRange bytes from to))