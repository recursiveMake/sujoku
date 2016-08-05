(ns sudoku.structure
  (:require [clojure.math.numeric-tower :as math]
            [sudoku.util :refer :all]))

(defn row-size
  "Size of rows in puzzle"
  [puzzle-count]
  (math/sqrt puzzle-count))
(def row-size-m (memoize row-size))

(defn group-size
  "Size of groups in puzzle"
  [puzzle-count]
  (math/sqrt (row-size puzzle-count)))
(def group-size-m (memoize group-size))

(defn row-of
  "Return row index based on piece index"
  ([index]
   (row-of index 81))
  ([index max-pos]
   (quot index (row-size-m max-pos))))

(defn col-of
  "Return column index based on piece index"
  ([index]
   (col-of index 81))
  ([index max-pos]
   (rem index (row-size-m max-pos))))

(defn square-of
  "Return square index based on piece index"
  ([index]
   (square-of index 81))
  ([index max-pos]
   (def row (row-of index max-pos))
   (def col (col-of index max-pos))
   (def group (group-size-m max-pos))
   (+ (* group (quot row group)) (quot col group))))

(defn row-ind-raw
  "Seq of indices that are in given row"
  ([r]
   (row-ind r 81))
  ([r max-pos]
   (def rows (row-size-m max-pos))
   (def start (* r rows))
   (range start (+ start rows))))
(def row-ind (memoize row-ind-raw))

(defn col-ind-raw
  "Seq of indices that are in given column"
  ([c]
   (col-ind c 81))
  ([c max-pos]
   (def cols (row-size-m max-pos))
   (range c max-pos cols)))
(def col-ind (memoize col-ind-raw))

(defn square-ind-raw
  "Seq of indices that are in a given square"
  ([s]
   (square-ind s 81))
  ([s max-pos]
   (def rows (row-size-m max-pos))
   (def grouping (group-size-m max-pos))
   (def r (quot s grouping))
   (def c (rem s grouping))
   (def start (+ (* rows grouping r) (* c grouping)))
   (def first-chunk (range start (+ start grouping)))
   (sort (flatten (map #(range % (+ % (* rows grouping)) rows) first-chunk)))))
(def square-ind (memoize square-ind-raw))

(defn neighbors-ind-raw
  "Generate indices of neighbors given piece index"
  ([index]
   (neighbors-ind index 81))
  ([index max-pos]
   (except index (sort (set (reduce into [(row-ind (row-of index max-pos) max-pos)
                                          (col-ind (col-of index max-pos) max-pos)
                                          (square-ind (square-of index max-pos) max-pos)]))))))
(def neighbors-ind (memoize neighbors-ind-raw))
