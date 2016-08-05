(ns sudoku.accessors
  (:require [sudoku.util :refer [except]]
            [sudoku.structure :refer :all]))

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
