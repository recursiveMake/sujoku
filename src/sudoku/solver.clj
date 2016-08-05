(ns sudoku.solver
  (:require [clojure.set]
            [sudoku.structure :refer :all]
            [sudoku.accessors :refer :all]
            [sudoku.util :refer [unique-values]]))

(defn solved?
  "Check if value of piece is known"
  [puzzle index]
  (not (= 0 (nth puzzle index))))

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
      (step puzzle))))

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
