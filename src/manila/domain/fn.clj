(ns manila.domain.fn)

(defn assoc-non-nil
  ([coll k v] (if (nil? v) coll (assoc coll k v)))
  ([coll k v & kvs]
   (let [ret (assoc-non-nil coll k v)]
     (if kvs
       (if (next kvs)
         (recur ret (first kvs) (second kvs) (nnext kvs))
         (throw (IllegalArgumentException.
                  (str "assoc-non-nil expects even number of arguments after "
                       "map/vector, found odd number"))))
       ret))))

(defn assoc-non-empty
  ([coll k v] (assoc-non-nil coll k (not-empty v)))
  ([coll k v & kvs]
   (let [ret (assoc-non-empty coll k v)]
     (if kvs
       (if (next kvs)
         (recur ret (first kvs) (second kvs) (nnext kvs))
         (throw (IllegalArgumentException.
                  (str "assoc-non-empty expects even number of arguments after "
                       "map/vector, found odd number"))))
       ret))))
