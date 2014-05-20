(ns hatch.slam_test
  ^{:author "Tom Dolan"
    :doc "Property-based tests (utilizing test.check) for the 'slam-related' functions
          in the 'hatch' namespace."}
  (:require [hatch :as h]
           [clojure.test.check :as tc]
           [clojure.test.check.generators :as gen]
           [clojure.test.check.properties :as prop]
           [clojure.test.check.clojure-test :refer [ defspec]]))




;;Testing the 'assumed truth' that the output of the slam function
;;is in fact a valid keyword.
(defspec slam-output-is-keyword
  100 ;; the number of iterations for test.check to test
  (prop/for-all [v (gen/vector gen/keyword 2)] ;;gen pairs of keywords
    (keyword? (h/slam (nth v 0) (nth v 1)))))

;; Attempt to use gen/bind, but actual use in test was not working.
;; Including here just to show the construct
(def keylist gen/keyword)
(def test-map
  (gen/bind keylist
            (fn map-gen [k]
              (gen/such-that not-empty (gen/map k (gen/such-that not-empty gen/string))))))

;;Testing the 'assumed truth' that the namespaced keyword is now
;;present in the map
(defspec slam-in-output-has-namespaced-key
  100
  (prop/for-all [gns (gen/such-that not-empty  gen/string) 
                 m (gen/such-that not-empty
                                  (gen/map gen/keyword (gen/such-that not-empty gen/string)))]
                (let [slammed-map (h/slam-in m gns (first (keys m)))]
                  ;; TODO check for namespaced key
                  (map? slammed-map))))

;;Testing assumed truth that output is a map
(defspec slam-all-output-is-valid-map
  100
  (prop/for-all [gns (gen/such-that not-empty  gen/string) 
                 m (gen/such-that not-empty
                                  (gen/map gen/keyword (gen/such-that not-empty gen/string)) )]
                (map? (h/slam-all m gns ))))
