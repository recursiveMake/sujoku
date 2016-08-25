(ns sudoku.core
  (:require [sudoku.io :refer [read-puzzle print-puzzle]]
            [sudoku.solver.parallel :refer [solve free-solve] :rename {solve parallel-solve}]
            [sudoku.solver.serial :refer [solve] :rename {solve serial-solve}])
  (:gen-class))


(def puzzle-string-81 "100030009020640080003200740200000000014000000000062800007000300080450020000000001")
(def puzzle-string-16 "1234000000000000")
(def puzzle-string-16-x "1234123412341230")
(def puzzle-string-256 "0 0 1 0 0 0 5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 4 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 7 0 0 0 0 5 1 0 0 0 0 0 0 0 16 0 0 0 10 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 0 0 0 0 0 3 9 0 0 0 6 0 4 0 0 0 0 10 0 0 10 0 0 0 0 0 0 0 0 0 0 0 14 11 0 0 0 0 11 6 0 12 0 0 1 0 3 14 0 13 5 0 0 0 7 4 0 11 0 0 0 14 0 0 0 0 15 0 0 0 1 0 0 0 0 0 2 0 11 0 0 3 0 0 0 0 3 13 2 0 1 0 0 0 5 0 0 0 4 0 0 12 0 0 10 0 0 0 0 0 0 0 16 14 0 0 0 0 0 8 0 0 0 0 0 0 9 0 0 0 0 0 0 0 13 0 0 7 0 0 0 0 0 0 0 6 0 0 0 0 0 14 0 13 0 0 0 2 4 0 0 15 0")

(def puzzle (read-puzzle puzzle-string-16))
