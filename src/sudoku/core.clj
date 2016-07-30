(ns sudoku.core
  (:require [clojure.math.numeric-tower :as math]
            [clojure.string :as string])
  (:gen-class))

(defn to-position
  [index]
  (inc index))

(defn to-index
  [position]
  (dec position))

(defn row-size
  "Size of rows in puzzle"
  [puzzle]
  (math/sqrt (count puzzle)))

(defn group-size
  "Size of groups in puzzle"
  [puzzle]
  (math/sqrt (row-size puzzle)))

(defn row-of
  "Return row index based on piece index"
  ([index]
   (row-of index 81))
  ([index max-pos]
   (def pieces-per-row (math/sqrt max-pos))
   (quot index pieces-per-row)))

(defn col-of
  "Return column index based on piece index"
  ([index]
   (col-of index 81))
  ([index max-pos]
   (def pieces-per-column (math/sqrt max-pos))
   (rem index pieces-per-column)))

(defn square-of
  "Return square index based on piece index"
  ([index]
   (square-of index 81))
  ([index max-pos]
   (def row (row-of index max-pos))
   (def col (col-of index max-pos))
   (def group (math/sqrt (math/sqrt max-pos)))
   (+ (* 3 (quot row group)) (quot col group))))

(defn row-ind
  "Seq of indices that are in given row"
  ([r]
   (row-ind r 81))
  ([r max-pos]
   (def rows (math/sqrt max-pos))
   (def start (* r rows))
   (range start (+ start rows))))

(defn col-ind
  "Seq of indices that are in given column"
  ([c]
   (col-ind c 81))
  ([c max-pos]
   (def cols (math/sqrt max-pos))
   (range c max-pos cols)))

(defn square-ind
  "Seq of indices that are in a given square"
  ([s]
   (square-ind s 81))
  ([s max-pos]
   (def rows (math/sqrt max-pos))
   (def grouping (math/sqrt rows))
   (def r (quot s grouping))
   (def c (rem s grouping))
   (def start (+ (* rows grouping r) (* c grouping)))
   (def first-chunk (range start (+ start grouping)))
   (sort (flatten (map #(range % (+ % (* rows grouping)) rows) first-chunk)))))

(defn puzzle-pieces
  "Get puzzle pieces by list of indices"
  [puzzle indices]
  (mapv puzzle indices))

(defn read-puzzle
  "Transform text puzzle into an seq on integers"
  [puzzle-string]
  (map #(Integer. %) (string/split puzzle-string #"")))

(defn row-to-string
  [row grouping]
  (string/join " | " (map #(string/join " " %) (partition grouping row))))

(defn row-group-to-string
  [rows]
  (def columns (count (first rows)))
  (def groups (math/sqrt columns))
  (conj (map row-to-string rows (repeat groups))
        (row-to-string (take columns (repeat "-")) groups)))

(defn puzzle-to-string
  [puzzle]
  (def rows (partition (row-size puzzle) puzzle))
  (def row-groups (partition (group-size puzzle) rows))
  (string/join "\n" (flatten (map row-group-to-string row-groups))))

(defn print-puzzle
  [puzzle]
  (print (puzzle-to-string puzzle)))
