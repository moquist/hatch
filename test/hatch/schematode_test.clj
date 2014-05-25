(ns hatch.slam_test
  ^{:author "Tom Dolan"
    :doc "Property-based tests (utilizing test.check and Herbert)
          for the 'schematode-related' functions in the 'hatch' namesspace."}
  (:require [hatch :as h]
           [clojure.test.check :as tc]
           [clojure.test.check.generators :as gen]
           [clojure.test.check.properties :as prop]
           [clojure.test.check.clojure-test :refer [ defspec]]
           [miner.herbert.generators :as hg]
           [miner.herbert.predicates :as hp]
           [miner.herbert.canonical :as hc]))

(def schematode-def
    [[:person {:attrs [[:name :string :db.unique/identity]
                       [:favorite-dessert :ref]]}]
     [:dessert {:attrs [[:name :string :db.unique/identity]]
                :part :desserts}]])

;;schematode representation is a:
;; vector of vectors (entity-types)
;;  each 'inner' vector contains a keyword and a map
;;   each map contains the keyword 'attrs' and a vector of attributes
;;    each attr vector must
;;     contain :db.unique/identity
;;     optionally contain :part

;; including temporarily just for reference of schematode definition
#_(def schematode-def
    [[:person {:attrs [[:name :string :db.unique/identity]
                       [:favorite-dessert :ref]]}]
     [:dessert {:attrs [[:name :string :db.unique/identity]]
                :part :desserts}]])

;; trying to setup a 'grammar' for definining/generating entity-types,
;; but the grammar fn in Herbert does not seem to be in the source for
;; the library on github (possible?)
#_(def entity-name
  (hg/grammar [entity+]
    entity-type (kw)
    attrs (map (kw ":attrs") [kw*])
    entity [entity-type attrs]))
