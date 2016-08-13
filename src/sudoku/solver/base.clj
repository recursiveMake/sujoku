(ns sudoku.solver.base
  (:require [clojure.set]
            [sudoku.structure :refer :all]
            [sudoku.accessors :refer :all]))

(defn piece-solved?
  "Check if value of piece is known"
  [puzzle index]
  (not (= 0 (nth puzzle index))))

(defn choices
  "Generate possible values for square in puzzle"
  ([puzzle index]
   (choices puzzle index (count puzzle)))
  ([puzzle index max-pos]
   (def all-choices (set (range 1 (inc (row-size-m max-pos)))))
   (clojure.set/difference all-choices (set (neighbors puzzle index max-pos)))))

(defn candidate-pieces
  "Generate a list of possible guesses"
  [puzzle]
  (def n-pieces (count puzzle))
  (def solutions #(if (piece-solved? puzzle %) nil (choices puzzle %)))
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

(defn percent-complete
  "Returns a fraction of the puzzle solved"
  [puzzle]
  (/ (count (filter #(not (= 0 %)) puzzle)) (count puzzle)))

(defn finished?
  "Check if all squares have been filled"
  [puzzle]
  (not (some #(= 0 %) puzzle)))

(def single-step #(first (new-puzzles-from-choices %1 %2)))
(def multi-step new-puzzles-from-choices)

