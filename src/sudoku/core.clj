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

(defn except
  "Remove a value from list"
  [index values]
  (remove #(= index %) values))

(defn neighbors-ind
  "Generate indices of neighbors given piece index"
  ([index]
   (neighbors-ind index 81))
  ([index max-pos]
   (except index (sort (set (reduce into [(row-ind (row-of index max-pos) max-pos)
                                                 (col-ind (col-of index max-pos) max-pos)
                                                 (square-ind (square-of index max-pos) max-pos)]))))))

(defn solved?
  "Check if value of piece is known"
  [puzzle index]
  (not (= 0 (nth puzzle index))))

(defn puzzle-pieces
  "Get puzzle pieces by list of indices"
  [puzzle indices]
  (map nth (repeat puzzle) indices))

(defn get-neighbors
  "Get neighboring piece values"
  ([of-function ind-function puzzle index max-pos]
   (puzzle-pieces puzzle (except index (ind-function (of-function index max-pos)))))
  )

(defn row-neighbors
  "Get values from neighboring cells"
  ([puzzle index]
   (row-neighbors puzzle index 81))
  ([puzzle index max-pos]
   (get-neighbors row-of row-ind puzzle index max-pos)))

(defn col-neighbors
  ([puzzle index]
   (col-neighbors puzzle index 81))
  ([puzzle index max-pos]
   (get-neighbors col-of col-ind puzzle index max-pos)))

(defn square-neighbors
  ([puzzle index]
   (square-neighbors puzzle index 81))
  ([puzzle index max-pos]
   (get-neighbors square-of square-ind puzzle index max-pos)))

(defn neighbors
  ([puzzle index]
   (neighbors puzzle index 81))
  ([puzzle index max-pos]
   (puzzle-pieces puzzle (neighbors-ind index max-pos))))

(defn all-choices
  ([]
   (all-choices 81))
  ([max-pos]
   (set (range 1 (inc (math/sqrt max-pos))))))

(defn choices
  ([puzzle index]
   (choices puzzle index 81))
  ([puzzle index max-pos]
   (clojure.set/difference (all-choices max-pos) (set (neighbors puzzle index max-pos)))))

(defn candidate-pieces
  "Generate a list of possible guesses"
  [puzzle]
  (def n-pieces (count puzzle))
  (def solutions #(if (solved? puzzle %) nil (choices puzzle %)))
  (def possible-solutions (zipmap (range 0 n-pieces)
                                  (map solutions (range 0 n-pieces))))
  (remove #(= nil (last %)) (sort-by #(count (last %)) possible-solutions)))

(defn new-puzzles-from-choices
  "Generate new puzzles with guessed fields"
  [puzzle [index choices]]
  (def puzzle-list (into [] puzzle))
  (map #(seq (assoc %1 %2 %3)) (repeat puzzle-list) (repeat index) choices)
)

(defn valid?
  [puzzle index]
  ;; TODO
)

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

(defn puzzle-to-print-string
  [puzzle]
  (def rows (partition (row-size puzzle) puzzle))
  (def row-groups (partition (group-size puzzle) rows))
  (string/replace (string/join "\n" (flatten (map row-group-to-string row-groups))) #"0" "*"))

(defn print-puzzle
  [puzzle]
  (print (puzzle-to-print-string puzzle)))

(defn puzzle-to-string
  [puzzle]
  (string/join "" puzzle))
