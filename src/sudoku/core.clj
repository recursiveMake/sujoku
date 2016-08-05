(ns sudoku.core
  (:require [sudoku.io :refer [read-puzzle print-puzzle]]
            [sudoku.solver :refer [solve]])
  (:gen-class))


(def puzzle-string-81 "100030009020640080003200740200000000014000000000062800007000300080450020000000001")
(def puzzle-string-16 "1234000000000000")
(def puzzle-string-16-x "1234123412341230")

(def puzzle (read-puzzle puzzle-string-16))
