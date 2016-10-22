(ns sudoku.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [sudoku.io :refer [read-puzzle puzzle-to-string puzzle-to-print-string]]
            [sudoku.solver.parallel :refer [solve free-solve] :rename {solve parallel-solve}]
            [sudoku.solver.serial :refer [solve] :rename {solve serial-solve}])
  (:gen-class))


;; (def puzzle-string-81 "100030009020640080003200740200000000014000000000062800007000300080450020000000001")
;; (def puzzle-string-16 "1234000000000000")
;; (def puzzle-string-16-x "1234123412341230")
;; (def puzzle-string-256 "0 0 1 0 0 0 5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 4 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 7 0 0 0 0 5 1 0 0 0 0 0 0 0 16 0 0 0 10 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 0 0 0 0 0 3 9 0 0 0 6 0 4 0 0 0 0 10 0 0 10 0 0 0 0 0 0 0 0 0 0 0 14 11 0 0 0 0 11 6 0 12 0 0 1 0 3 14 0 13 5 0 0 0 7 4 0 11 0 0 0 14 0 0 0 0 15 0 0 0 1 0 0 0 0 0 2 0 11 0 0 3 0 0 0 0 3 13 2 0 1 0 0 0 5 0 0 0 4 0 0 12 0 0 10 0 0 0 0 0 0 0 16 14 0 0 0 0 0 8 0 0 0 0 0 0 9 0 0 0 0 0 0 0 13 0 0 7 0 0 0 0 0 0 0 6 0 0 0 0 0 14 0 13 0 0 0 2 4 0 0 15 0")
(defn validate-puzzle
  [puzzle]
  (contains? #{16 81 256} (count puzzle)))

(def solvers (hash-map
              "serial" serial-solve
              "parallel" parallel-solve
              "free" free-solve))

(def options
  ;; Accepted command line options
  [["-p" "--puzzle" "Puzzle to solve"
    :parse-fn read-puzzle
    :validate [validate-puzzle "Puzzle is invalid"]
    :required "PUZZLE"]
   ["-s" "--solver" (str "Solver to use: " (clojure.string/join ", " (keys solvers)))
    :default "serial"
    :required "SOLVER"
    :parse-fn clojure.string/trim
    :validate [#(contains? solvers %)
               (str "Must be one of valid options: "
                    (clojure.string/join ", " (keys solvers)))]]
   ["-b" "--brief" "Print puzzle in compact mode"]
   ["-l" "--log" "Enable logging"]
   ["-h" "--help" "Print this help message"]])

(def required-opts #{:puzzle})

(defn missing-required?
  [opts]
  (not-every? opts required-opts))

(defn do-solve
  "Execute solver with required parameters"
  [opts]
  (def solution ((solvers (:solver opts)) (:puzzle opts)))
  (if (:brief opts)
    (puzzle-to-string solution)
    (puzzle-to-print-string solution)))

(defn usage
  [summary]
  (clojure.string/join \newline
               ["Here are your options"
                ""
                summary]))
(defn exit
  [status message]
  (println message)
  (System/exit status))

(defn error-msg
  [errors]
  (str "The following errors occured" \newline
       (clojure.string/join \newline errors))
)

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args options)]
    (cond (:help options) (exit 0 (usage summary))
          errors (exit 1 (error-msg errors))
          (missing-required? options) (exit 1 (usage summary))
          :else (println (do-solve options))))
  (System/exit 0))
