(ns manila.domain)

(derive java.lang.String ::string)
(derive clojure.lang.Keyword ::string)
(derive clojure.lang.Symbol ::string)

(derive java.lang.Boolean ::bool)

(derive java.lang.Long ::number)
(derive java.lang.Double ::number)
(derive clojure.lang.BigInt ::number)
(derive java.math.BigDecimal ::number)

(defmulti ^:private matches-attribute?
  (fn [field attribute] (::data-type field)))

(defmethod matches-attribute? ::string
  [_ {::keys [value]}]
  (isa? value ::string))

(defmethod matches-attribute? ::number
  [_ {::keys [value]}]
  (isa? value ::number))

(defmethod matches-attribute? ::bool
  [_ {::keys [value]}]
  (isa? value ::bool))

(defmethod matches-attribute? ::nil
  [_ {::keys [value]}]
  (nil? value))

(defmethod matches-attribute? ::union
  [field attr]
  (some #(matches-attribute? % attr) (::children field)))

(defn ^:private get-val
  [k coll]
  (when-first [pair (filter #(= k (::key %)) coll)]
    (::value pair)))

(defn ^:private type-violations
  [obj schema]
  (keep
    (fn [{attr-key ::key attr ::value}]
      (when-let [field (get-val attr-key (::fields schema))]
        (when-not (matches-attribute field attribute?)
          {::schema-violation ::type-mismatch
           ::attribute attr
           ::field field}))
      (::attributes obj))))

(defn ^:private attributes-not-in-schema
  [obj schema]
  (let [schema-keys (map ::key (::fields schema))]
    (keep
      (fn [{obj-key ::key :as attr}]
        (when (not-any? #{obj-key} schema-keys)
          {::schema-violation ::attr-not-in-schema
           ::attribute attr}))
      (::attributes obj))))

(defn ^:private fields-not-in-object
  [obj schema]
  (let [object-keys (map ::key (::attributes obj))]
    (keep
      (fn [{field-key ::key :as field}]
        (when (and (nil? (::default field))
                   (not-any? #{field-key} object-keys))
          {::schema-violation ::attr-missing
           ::attribute-key field-key}))
      (::fields schema))))

(defn schema-violations
  [{::keys [object schema]}]
  (not-empty
    (concat (type-violations object schema)
            (attributes-not-in-schema object schema)
            (fields-not-in-object object schema))))
