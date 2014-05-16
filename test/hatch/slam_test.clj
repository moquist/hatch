(ns hatch.slam_test
  ^{:author "Tom Dolan"
    :doc "Property-based tests (utilizing test.check) for the 'slam-related' functions
          in the 'hatch' namespace."}
  (:require [hatch :as h]
           [clojure.test.check :as tc]
           [clojure.test.check.generators :as gen]
           [clojure.test.check.properties :as prop]
           [clojure.test.check.clojure-test :refer [ defspec]]))

(defspec slam-output-is-keyword
  "Testing the 'assumed truth' that the output of the slam function
   is in fact a valid keyword."
  100 ;; the number of iterations for test.check to test
  (prop/for-all [v (gen/vector gen/keyword 2)] ;;gen pairs of keywords
    (keyword? (h/slam (nth v 0) (nth v 1)))))


