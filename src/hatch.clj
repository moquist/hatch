(ns hatch
  ^{:author "David Zaharee <dzaharee@vlacs.org>"
    :doc "Useful functions for working with Datomic and Schematode"}
  (:require [datomic.api :as d]
            [datomic-schematode.core :as schematode]))

(defn slam
  "Slams two keywords together into one namespaced keyword"
  [ns n]
  (keyword (name ns) (name n)))

(defn prefix-keys
  "Prefix all keys in a map with prefix"
  [m prefix]
  (into {} (for [[k v] m] [(slam prefix k) v])))
