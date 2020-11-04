(ns manila.api.json-api
  (:require [manila.domain :as d]
            [manila.domain.fn :refer [assoc-non-nil assoc-non-empty]]))

;; forward-references
(declare manila->resource)
;; end forward-references

(defn ^:private id
  [obj]
  (str (::d/id obj)))

(defn ^:private type-name
  [obj]
  (::d/type obj))

(defn ^:private attributes
  [obj]
  (let [start {::name (::d/name obj)
               ::human-readable-name (::d/human-readable-name obj)
               ::comment (::d/comment obj)}
        kv->json-api (fn [kv] [(::d/key kv) (::d/value kv)])
        kvs (map kv->json-api (::d/attributes obj))]
    (apply conj start kvs)))

(defn ^:private relationships
  [obj]
  (when (contains? obj ::d/parent-folder)
    {::parent
     {::data
      (when-let [parent-id (get-in obj [::d/parent-folder ::d/id])]
        {::type ::d/folder ::id parent-id})}}))

(defn ^:private included
  [obj]
  (let [parent (::d/parent-folder obj)]
    (when (not-every? nil? (vals (attributes parent)))
      [(manila->resource parent)])))

(defn manila->resource-identifier
  [obj]
  {::type (type-name obj) ::id (id obj)})

(defn manila->resource
  [obj]
  (-> (manila->resource-identifier obj)
      (assoc-non-nil ::attributes (attributes obj))
      (assoc-non-nil ::relationships (relationships obj))))

(defn manila->json-api
  [obj]
  (-> {::data (manila->resource obj)}
      (assoc-non-empty ::included (included obj))))
