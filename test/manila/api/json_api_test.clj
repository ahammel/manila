(ns manila.api.json-api-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [manila.api.json-api :refer :all]
            [manila.domain :as d]))

(defn ^:private serializes-to
  [{:keys [actual expected]}]
  (is (= (json/write-str actual)
         (json/write-str expected))))

(deftest manila-2-json-api
  (testing "no attributes, no parent folder"
    (serializes-to
      {:actual
       (manila->json-api {::d/type "foo" ::d/id "bar"})
       :expected
       {:data
        {:type "foo"
         :id "bar"
         :attributes
         {:name nil
          :human-readable-name nil
          :comment nil}}}}))

  (testing "no attributes, with parent folder"
    (serializes-to
      {:actual
       (manila->json-api {::d/type "foo"
                          ::d/id "bar"
                          ::d/parent-folder {::d/id "parent"}})
       :expected
       {:data
        {:type "foo"
         :id "bar"
         :attributes
         {:name nil
          :human-readable-name nil
          :comment nil}
         :relationships
         {:parent {:data {:type "folder" :id "parent"}}}}}}))

  (testing "root folder"
    (serializes-to
      {:actual
       (manila->json-api {::d/type "folder"
                          ::d/id "root"
                          ::d/name "Root"
                          ::d/parent-folder nil})
       :expected
       {:data
        {:type "folder"
         :id "root"
         :attributes
         {:name "Root"
          :human-readable-name nil
          :comment nil}
         :relationships
         {:parent {:data nil}}}}}))

  (testing "with name, human-readable-name, and comment"
    (serializes-to
      {:actual
       (manila->json-api {::d/type "foo"
                          ::d/id "bar"
                          ::d/parent-folder {::d/id "parent"}
                          ::d/name "my-great-document"
                          ::d/human-readable-name "My Great Document"
                          ::d/comment "This document is great"})
       :expected
       {:data
        {:type "foo"
         :id "bar"
         :attributes
         {:name "my-great-document"
          :human-readable-name "My Great Document"
          :comment "This document is great"}
         :relationships
         {:parent {:data {:type "folder" :id "parent"}}}}}}))

  (testing "with parent folder included"
   (serializes-to
      {:actual
       (manila->json-api {::d/type "foo"
                          ::d/id "bar"
                          ::d/parent-folder {::d/id "parent"
                                             ::d/type "folder"
                                             ::d/name "root"
                                             ::d/parent-folder nil}
                          ::d/name "my-great-document"
                          ::d/human-readable-name "My Great Document"
                          ::d/comment "This document is great"})
       :expected
       {:data
        {:type "foo"
         :id "bar"
         :attributes
         {:name "my-great-document"
          :human-readable-name "My Great Document"
          :comment "This document is great"}
         :relationships
         {:parent {:data {:type "folder" :id "parent"}}}}
         :included
         [{:type "folder"
           :id "parent"
           :attributes
           {:name "root"
            :human-readable-name nil
            :comment nil}
           :relationships
           {:parent
            {:data nil}}}]}}))

  (testing "with add-on attributes"
   (serializes-to
      {:actual
       (manila->json-api {::d/type "foo"
                          ::d/id "bar"
                          ::d/parent-folder {::d/id "parent"
                                             ::d/type "folder"}
                          ::d/name "my-great-document"
                          ::d/human-readable-name "My Great Document"
                          ::d/comment "This document is great"
                          ::d/attributes
                          [{::d/key :favorite-fruit ::d/value "banana"}]})
       :expected
       {:data
        {:type "foo"
         :id "bar"
         :attributes
         {:name "my-great-document"
          :human-readable-name "My Great Document"
          :comment "This document is great"
          :favorite-fruit "banana"}
         :relationships
         {:parent {:data {:type "folder" :id "parent"}}}}}})))


