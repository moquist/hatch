(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [datomic.api :as d]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [hatch.test-config :as ht-config]
            [datomic-schematode.core :as schematode]))

(defn go
  "Initializes the current development system and starts it running."
  []
  (ht-config/start!))

(defn reset []
  (ht-config/stop!)
  (refresh :after 'user/go))

(defn touch-that
  "Execute the specified query on the current DB and return the
   results of touching each entity.

   The first binding must be to the entity.
   All other bindings are ignored."
  [query & data-sources]
  (map #(d/touch
         (d/entity
          (d/db (:db-conn ht-config/system))
          (first %)))
       (apply d/q query (d/db (:db-conn ht-config/system)) data-sources)))

(defn ptouch-that
  "Example: (ptouch-that '[:find ?e :where [?e :user/username]])"
  [query & data-sources]
  (pprint (apply touch-that query data-sources)))

(comment

  )
