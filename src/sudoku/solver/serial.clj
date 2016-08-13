(ns sudoku.solver.serial
  (:require [sudoku.solver.base :refer :all]))

(defn walk-solution-tree
  "Depth first search of a puzzle
  Returns a vector of:
    * solution if found
    * multiple solutions if at a branch
    * nil if puzzle is invalid"
  [puzzle]
  (loop [new-puzzle puzzle]
    (def possible-choice (candidate-piece new-puzzle))
    (if (= nil possible-choice)
      (vector new-puzzle)
      (if (= 1 (count (last possible-choice)))
        (recur (single-step new-puzzle possible-choice))
        (multi-step new-puzzle possible-choice)))))

(defn manage-solutions
  "Add new puzzles to puzzles list"
  [puzzles solutions]
  (if (= (vector nil) solutions)
    (rest puzzles)
    (into solutions (rest puzzles))))
(defn solve
  "Find a solution to puzzle if one exists"
  [puzzle]
  (loop [puzzles [puzzle]]
    (if (finished? (first puzzles))
      (first puzzles)
      (recur (manage-solutions puzzles (walk-solution-tree (first puzzles)))))))
