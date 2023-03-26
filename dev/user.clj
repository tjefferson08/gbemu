(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-repl-state]
            [gbemu.system :as system]))

(ig-repl/set-prep! (fn [] system/config))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(comment
  (go)
  (halt)
  (reset)

  ig-repl/go

  ig-repl-state/system)
