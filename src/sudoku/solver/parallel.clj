(ns sudoku.solver.parallel
  (:require [sudoku.solver.base :refer :all]
            [sudoku.util :refer [except atomic-first atomic-push]]
            [clojure.tools.logging :as log]))

(defn solve
  "Find a solution to puzzle (multithreaded)
  Depth first search until choice
  Syncs after each loop
  No sorting of solutions
  Continues in multiple threads based on concurrency"
  ([puzzle]
   (solve puzzle 3))
  ([puzzle concurrency]
   (loop [puzzles [puzzle]]
     (let [attempts (take concurrency puzzles)
           remaining (drop concurrency puzzles)
           results (map #(future (walk-solution-tree %)) attempts)
           possible-solutions (except nil (reduce concat (map deref results)))
           ;; Sorting solutions causes breadth first search in large puzzles
           ;; all-solutions (reverse (sort-by percent-complete (distinct (concat possible-solutions remaining))))
           all-solutions (concat possible-solutions remaining)]
       (if (finished? (first all-solutions))
         (first all-solutions)
         (recur all-solutions))))))

(defn start-solution-loop
  "Depth first search for a solution
  Adds generated choices to puzzles atom
  Terminates when nil or realized"
  [puzzle solution atom]
  (loop [current-puzzle puzzle]
    (if (not (realized? solution))
      (let [solutions (walk-solution-tree current-puzzle)
            new-puzzle (first solutions)
            candidate-puzzles (rest solutions)]
        (if (not (nil? new-puzzle))
          (if (finished? new-puzzle)
            (deliver solution new-puzzle)
            (do
              (atomic-push atom candidate-puzzles)
              (recur new-puzzle))))))))

(defn start-solution-thread
  "Hands off puzzles to a solution loop
  Stops when a solution is realized"
  [puzzles solution]
  (loop []
    (if (not (realized? solution))
      (let [current-puzzle (atomic-first puzzles)]
        (if (not (nil? current-puzzle))
          (start-solution-loop current-puzzle solution puzzles))
        (recur)))))

(defn free-solve
  "Find a solution to puzzle (multithreaded)
  No syncing
  No sorting of solutions
  (How to order solutions?)
  Continues in multiple threads"
  ([puzzle]
   (free-solve puzzle 3))
  ([puzzle concurrency]
   (let [
         solution (promise)       ; placeholder for solution
         puzzles (atom [puzzle])  ; atomic list of puzzles to be solved
         ]
     (doall (map #(future (start-solution-thread %1 %2))
                 (repeat concurrency puzzles)
                 (repeat concurrency solution)))
     (deref solution))))
