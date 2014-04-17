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

(defn get-partition
  "Get the db partition entity type is in from a schematode definition."
  [schema entity-type]
  (or (get-in schema [entity-type :part])
      :db.part/user))

(defn ensure-db-id
  "Ensure an entity has a db/id using tempid."
  ([part entity]
     (if (nil? (:db/id entity))
       (merge {:db/id (d/tempid part)} entity)
       entity))
  ([schema entity-type entity]
     (ensure-db-id (get-partition schema entity-type) entity)))

(defn prune-entity
  "Prune an entity using attr-map and entity-type."
  [attr-map entity-type entity]
  (select-keys entity (conj (entity-type attr-map) :db/id)))

(defn tx!
  "Transact with schematode constraints"
  [db-conn txs]
  (schematode/tx db-conn :enforce txs))

(defn tx-clean-entity!
  "Clean up an entity (ensure it has a db/id and prune it), then transact it."
  [schema attr-map db-conn entity-type entity]
  (->> entity
       (ensure-db-id schema entity-type)
       (prune-entity attr-map entity-type)
       (conj [])
       (tx! db-conn)))

(comment

  (def schematode-def
    [[:person {:attrs [[:name :string :db.unique/identity]
                       [:favorite-dessert :ref]]}]
     [:dessert {:attrs [[:name :string :db.unique/identity]]
                :part :desserts}]])

  (def valid-attrs {:person [:person/name :person/favorite-dessert]
                    :dessert [:dessert/name]})

  (def tx-entity! (partial hatch/tx-clean-entity! schematode-def valid-attrs))

  (schematode/init-schematode-constraints! (:db-conn ht-config/system))
  (schematode/load-schema! (:db-conn ht-config/system) schematode-def)
  (tx-entity! (:db-conn ht-config/system) :dessert {:dessert/name "ice cream"})
  (tx-entity! (:db-conn ht-config/system)
             :person
             {:person/name "Jon" :person/favorite-dessert [:dessert/name "ice cream"]})

)
