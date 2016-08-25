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

;; From http://stackoverflow.com/a/22409846
(defn swap*!
  "Like swap! but returns a vector of [old-value new-value]"
  [atom f & args]
  (loop [] 
    (let [ov @atom 
          nv (apply f ov args)]
      (if (compare-and-set! atom ov nv)
        [ov nv]
        (recur)))))

(defn atomic-first [atom]
  (let [[ov nv] (swap*! atom subvec 1)]
    (first ov)))

; loop
;  -> atomic-pop puzzles
;   = puzzle
;  -> do-solve puzzle
;   = solutions
;  -> sort-solutions solutions
;    --> finished?
;       --> deliver first solutions
;       --> strip-nils solutions
;          --> swap! puzzles solutions
;  -> realized? solution
;    --> recur puzzles

(defn start-solution-thread
  [
   puzzles  ; atomic list of possible puzzles
   solution ; unrealized promise of a solution
   ]
  (loop []
    (log/info "Recurring solution thread")
    (if (not (realized? solution))
      (let [current-puzzle (atomic-first puzzles)]
        (log/info "Working on" current-puzzle "...")
        (if (= nil current-puzzle)
          (recur)
          (let [solutions (walk-solution-tree current-puzzle)]
            (log/info "Found" (count solutions) "new solutions")
            (if (= nil solutions)
              (recur)
              (if (finished? (first solutions))
                (deliver solution (first solutions))
                (do (swap! puzzles into solutions)
                    (recur))))))))))

(defn free-solve
  "Find a solution to puzzle (multithreaded)
  No syncing
  No sorting of solutions
  Continues in multiple threads"
  [puzzle]
  (let [
        concurrency 3            ; number of threads to start
        solution (promise)       ; placeholder for solution
        puzzles (atom [puzzle])  ; atomic list of puzzles to be solved
        ]
    (doall (map #(future (start-solution-thread %1 %2))
                (repeat concurrency puzzles)
                (repeat concurrency solution)))
    (log/info "Waiting for solutions on" concurrency "threads")
    (deref solution)))
