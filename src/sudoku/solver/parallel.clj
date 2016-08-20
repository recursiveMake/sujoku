(ns sudoku.solver.parallel
  (:require [sudoku.solver.base :refer :all]
            [sudoku.util :refer [except]]
            [clojure.tools.logging :as log]))

(defn solve
  "Find a solution to puzzle (multithreaded)
  Depth first search until choice
  Syncs after each loop
  No sorting of solutions
  Continues in multiple threads based on concurrency"
  ([puzzle]
   (solve puzzle 10))
  ([puzzle concurrency]
   (loop [puzzles [puzzle]]
     (let [attempts (take concurrency puzzles)
           remaining (drop concurrency puzzles)
           results (map #(future (walk-solution-tree %)) attempts)
           possible-solutions (except nil (reduce concat (map deref results)))
           ;; Sorting solutions causes breadth first search in large puzzles
           ;; all-solutions (reverse (sort-by percent-complete (distinct (concat possible-solutions remaining))))
           all-solutions (concat possible-solutions remaining)]
       ;; (log/info "IN:" (count puzzles)
       ;;           "SOLS:" (count possible-solutions)
       ;;           "REM:" (count remaining)
       ;;           "ALL:" (count all-solutions)
       ;;           "PER:" (float (percent-complete (first all-solutions))))
       (if (finished? (first all-solutions))
         (first all-solutions)
         (recur all-solutions))))))
