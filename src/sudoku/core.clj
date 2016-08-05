(ns sudoku.core
  (:require [clojure.math.numeric-tower :as math]
            [clojure.string :as string])
  (:gen-class))

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

(defn row-ind
  "Seq of indices that are in given row"
  ([r]
   (row-ind r 81))
  ([r max-pos]
   (def rows (row-size-m max-pos))
   (def start (* r rows))
   (range start (+ start rows))))

(defn col-ind
  "Seq of indices that are in given column"
  ([c]
   (col-ind c 81))
  ([c max-pos]
   (def cols (row-size-m max-pos))
   (range c max-pos cols)))

(defn square-ind
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
   (row-neighbors puzzle index (count puzzle)))
  ([puzzle index max-pos]
   (get-neighbors row-of row-ind puzzle index max-pos)))

(defn col-neighbors
  ([puzzle index]
   (col-neighbors puzzle index (count puzzle)))
  ([puzzle index max-pos]
   (get-neighbors col-of col-ind puzzle index max-pos)))

(defn square-neighbors
  ([puzzle index]
   (square-neighbors puzzle index (count puzzle)))
  ([puzzle index max-pos]
   (get-neighbors square-of square-ind puzzle index max-pos)))

(defn neighbors
  ([puzzle index]
   (neighbors puzzle index (count puzzle)))
  ([puzzle index max-pos]
   (puzzle-pieces puzzle (neighbors-ind index max-pos))))

(defn unique-values
  [list]
  (set (except 0 list)))

(defn all-choices
  ([]
   (all-choices 81))
  ([max-pos]
   (set (range 1 (inc (row-size-m max-pos))))))

(defn choices
  ([puzzle index]
   (choices puzzle index (count puzzle)))
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

(def candidate-piece #(first (candidate-pieces %)))

(defn new-puzzles-from-choices
  "Generate new puzzles with guessed fields"
  [puzzle [index choices]]
  (def puzzle-list (into [] puzzle))
  (map #(seq (assoc %1 %2 %3)) (repeat puzzle-list) (repeat index) choices)
)

(defn valid?
  "Check if a given index is valid with current puzzle"
  [puzzle index]
  (def value (nth puzzle index))
  (and (not (= 0 value)) 
       (not (contains? (unique-values (neighbors puzzle index)) value))))

(defn finished?
  "Check if all squares have been filled"
  [puzzle]
  (not (some #(= 0 %) puzzle)))

(defn invalid-puzzle?
  [puzzle]
  (= 0 (count (last (candidate-piece puzzle)))))

(defn step
  [puzzle]
  (if (finished? puzzle)
    puzzle
    (if (= 1 (count (last (candidate-piece puzzle))))
      (first (new-puzzles-from-choices puzzle (candidate-piece puzzle)))
      puzzle)))

(defn solve-until-choice
  "Step until guesses completed"
  [puzzle]
  (if (finished? puzzle)
    puzzle
    (if (invalid-puzzle? puzzle)
      nil
      (recur (step puzzle)))))

(defn solve-with-branching
  "Returns series of possible solutions or nil for broken puzzle"
  [puzzle]
  (def stuck-puzzle (solve-until-choice puzzle))
  (if (= nil stuck-puzzle)
    nil
    (if (finished? stuck-puzzle)
      (vector stuck-puzzle)
      (new-puzzles-from-choices stuck-puzzle (candidate-piece stuck-puzzle)))))

(defn manage-branched-solutions
  "Takes multiple puzzles and generates a list of branched solutions"
  [puzzles]
  (def possible-solutions (solve-with-branching (first puzzles)))
  (if (= nil possible-solutions)
    (rest puzzles)
    (into possible-solutions (rest puzzles))))

(defn solve
  "Find a solution to puzzle if one exists"
  [puzzle]
  (loop [puzzles [puzzle]]
    (if (finished? (first puzzles))
      (first puzzles)
      (recur (manage-branched-solutions puzzles)))))

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
  (def rows (partition (row-size-m (count puzzle)) puzzle))
  (def row-groups (partition (group-size-m (count puzzle)) rows))
  (string/replace (string/join "\n" (flatten (map row-group-to-string row-groups))) #"0" "*"))

(defn print-puzzle
  [puzzle]
  (print (puzzle-to-print-string puzzle)))

(defn puzzle-to-string
  [puzzle]
  (string/join "" puzzle))

(def puzzle-string-81 "100030009020640080003200740200000000014000000000062800007000300080450020000000001")
(def puzzle-string-16 "1234342121430000")
(def puzzle-string-16-x "1234123412341230")

(def puzzle (read-puzzle puzzle-string-16))
(println "Initial:")
(print-puzzle puzzle)
(println "\nFinal:")
(print-puzzle (solve puzzle))
