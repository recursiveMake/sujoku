(ns sudoku.util)

(defn except
  "Remove a value from list"
  [index values]
  (remove #(= index %) values))

(defn unique-values
  [list]
  (set (except 0 list)))
