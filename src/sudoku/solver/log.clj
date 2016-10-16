(ns sudoku.solver.log
  (:require [sudoku.util :refer [atomic-first atomic-push]]))

(defn save
  "Save a step in sudoku solving"
  [atom puzzle]
  (atomic-push atom [puzzle])
)
