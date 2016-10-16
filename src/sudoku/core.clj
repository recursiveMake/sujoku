(ns sudoku.core
  (:require [clojure.tools.cli :refer [cli]]
            [sudoku.io :refer [read-puzzle puzzle-to-string puzzle-to-print-string]]
            [sudoku.solver.parallel :refer [solve free-solve] :rename {solve parallel-solve}]
            [sudoku.solver.serial :refer [solve] :rename {solve serial-solve}])
  (:gen-class))


;; (def puzzle-string-81 "100030009020640080003200740200000000014000000000062800007000300080450020000000001")
;; (def puzzle-string-16 "1234000000000000")
;; (def puzzle-string-16-x "1234123412341230")
;; (def puzzle-string-256 "0 0 1 0 0 0 5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 4 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 7 0 0 0 0 5 1 0 0 0 0 0 0 0 16 0 0 0 10 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 0 0 0 0 0 3 9 0 0 0 6 0 4 0 0 0 0 10 0 0 10 0 0 0 0 0 0 0 0 0 0 0 14 11 0 0 0 0 11 6 0 12 0 0 1 0 3 14 0 13 5 0 0 0 7 4 0 11 0 0 0 14 0 0 0 0 15 0 0 0 1 0 0 0 0 0 2 0 11 0 0 3 0 0 0 0 3 13 2 0 1 0 0 0 5 0 0 0 4 0 0 12 0 0 10 0 0 0 0 0 0 0 16 14 0 0 0 0 0 8 0 0 0 0 0 0 9 0 0 0 0 0 0 0 13 0 0 7 0 0 0 0 0 0 0 6 0 0 0 0 0 14 0 13 0 0 0 2 4 0 0 15 0")

(defn select-solver
  [s]
  (case s
    "serial" serial-solve
    "parallel" parallel-solve
    "free" free-solve))

(def app-specs [["-p" "--puzzle" "Puzzle to solve"
                 :parse-fn read-puzzle]
                ["-s" "--solver" "Solver to use (serial, parallel, free)"
                 :default serial-solve
                 :parse-fn select-solver]
                ["-b" "--brief" "Print puzzle in compact mode"
                 :default false :flag true]
                ["-l" "--log" "Enable logging"
                 :default false :flag true]
                ["-h" "--help" "Print this help message"
                 :default false :flag true]])

(def required-opts #{:puzzle})

(defn missing-required?
  [opts]
  (not-every? opts required-opts))

(defn do-solve
  "Execute solver with required parameters"
  [opts]
  (def solution ((:solver opts) (:puzzle opts)))
  (if (:brief opts)
    (puzzle-to-string solution)
    (puzzle-to-print-string solution)))

(defn -main
  [& args]
  (let [
        [opts args banner] (apply cli args app-specs)]
    (if (or (:help opts)
              (missing-required? opts))
      (println banner)
      (println (do-solve opts)))))
