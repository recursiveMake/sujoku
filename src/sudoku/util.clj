(ns sudoku.util)

(defn except
  "Remove a value from list"
  [index values]
  (remove #(= index %) values))

(defn unique-values
  [list]
  (set (except 0 list)))

;; From http://stackoverflow.com/a/22409846
(defn swap*!
  "Like swap! but returns a vector of [old-value new-value]"
  [atom f & args]
  (loop []
    (let [ov @atom
          nv (apply f ov args)]
      (if (compare-and-set! atom ov nv)
        [ov nv]
        (recur)))))

(defn atomic-first
  [atom]
  (let [[ov nv] (swap*! atom subvec 1)]
    (first ov)))

(defn atomic-push
  "Add new puzzles to list (non-blocking)"
  [list items]
  (if (not (= 0 (count items)))
    (future (swap! list into items))))
