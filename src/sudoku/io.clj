(ns sudoku.io
  (:require [clojure.string :as string]
            [clojure.math.numeric-tower :as math]
            [sudoku.structure :refer :all]
            [sudoku.util :refer [except]]))

(defn read-puzzle
  "Transform text puzzle into an seq on integers"
  ([puzzle-string]
   (if (string/includes? puzzle-string " ")
     (read-puzzle puzzle-string #" ")
     (read-puzzle puzzle-string #"")))
  ([puzzle-string separator]
   (map #(Integer. %) (except "" (string/split puzzle-string separator)))))

(defn row-to-string
  [row grouping]
  (string/join " | " (map #(string/join " " %) (partition grouping row))))

(defn row-group-to-string
  [rows]
  (def columns (count (first rows)))
  (def groups (math/sqrt columns))
  (conj (map row-to-string rows (repeat groups))
        (row-to-string (take columns (repeat "-")) groups)))

(defn puzzle-to-print-string
  [puzzle]
  (def puzzle-rows (partition (row-size-m (count puzzle)) puzzle))
  (def row-groups (partition (group-size-m (count puzzle)) puzzle-rows))
  (def puzzle-string (string/join "\n" (flatten (map row-group-to-string row-groups))))
  (-> puzzle-string
      (string/replace #"10" "A")
      (string/replace #"11" "B")
      (string/replace #"12" "C")
      (string/replace #"13" "D")
      (string/replace #"14" "E")
      (string/replace #"15" "F")
      (string/replace #"16" "G")
      (string/replace #"0" "*")))

(defn print-puzzle
  [puzzle]
  (print (puzzle-to-print-string puzzle)))

(defn puzzle-to-string
  [puzzle]
  (string/join " " puzzle))
