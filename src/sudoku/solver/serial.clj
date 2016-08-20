(ns sudoku.solver.serial
  (:require [sudoku.solver.base :refer :all]))

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
