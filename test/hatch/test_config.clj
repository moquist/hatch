(ns hatch.test-config
  (:require [datomic.api :as d]))

(def system {:datomic-uri "datomic:mem://hatch-test"})

(defn start-datomic! [system]
  (d/create-database (:datomic-uri system))
  (assoc system :db-conn
         (d/connect (:datomic-uri system))))

(defn stop-datomic! [system]
  (dissoc system :db-conn)
  (d/delete-database (:datomic-uri system))
  system)

(defn start!
  "Starts the current development system."
  []
  (alter-var-root #'system start-datomic!))

(defn stop!
  "Shuts down and destroys the current development system."
  []
  (alter-var-root #'system
                  (fn [s] (when s (stop-datomic! s)))))

(defn testing-fixture [f]
  (start!)
  (f)
  (stop!))
