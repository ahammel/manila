(ns manila.domain-test
  (:require [clojure.test :refer :all]
            [manila.domain :as d :refer :all]))

(defn ^:private schema-violations' [& args]
  (into [] (apply schema-violations args)))

(deftest test-schema-violations
  (testing "valid schema"
    (is (nil? (schema-violations'
                {::d/object {::d/attributes
                            [{::d/key :fruit
                              ::d/value "banana"}]}
                 ::d/schema {::d/fields
                            [{::d/key :fruit
                              ::d/value {::d/data-type ::d/string}}]}}))))
  (testing "type violation"
    (is (= [{::d/schema-violation ::d/type-mismatch}]
           (schema-violations'
             {::d/object {::d/attributes
                         [{::d/key :fruit
                           ::d/value "banana"}]}
              ::d/schema {::d/fields
                         [{::d/key :fruit
                           ::d/value {::d/data-type ::d/number}}]}}))))
  (testing "too many attributes"
    (is (= []
           (schema-violations'
                {::d/object {::d/attributes
                            [{::d/key :fruit
                              ::d/value "banana"}
                             {::d/key ::dalmatians
                              ::d/value 101}]}
                 ::d/schema {::d/fields
                            [{::d/key :fruit
                              ::d/value {::d/data-type ::d/string}}]}}))))
  (testing "not enough attributes"
    (is (= [#:manila.domain{:schema-violation :manila.domain/type-mismatch, :attribute "banana", :field #:manila.domain{:data-type :manila.domain/string}} #:manila.domain{:schema-violation :manila.domain/attr-missing, :attribute-key :manila.domain-test/dalmatians}]
           (schema-violations'
                {::d/object {::d/attributes
                            [{::d/key :fruit ::d/value "banana"}]}
                 ::d/schema {::d/fields
                            [{::d/key :fruit
                              ::d/value {::d/data-type ::d/string}}
                             {::d/key ::dalmatians
                              ::d/value {::d/data-type ::d/number}}]}})))))
