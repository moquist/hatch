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
  "Prefix all keys in a map with prefix.
   Generally use namespaced attributes in your code.

   prefix-keys is there for you when you have non-namespaced
   attributes, such as when you get data from an external system."
  [m prefix]
  (into {} (for [[k v] m] [(slam prefix k) v])))

(defn schematode->partitions
  "Make a hatch partition map from your schematode definition.

   If you find yourself adding unnecessary things to your schema, you
   should instead define the map by hand."
  [schematode-schema]
  (into {} (for [[k v] schematode-schema] [k (slam :db.part (or (:part v) :user))])))

(defn schematode->attrs
  "Make a hatch attribute pruning map from your schematode
   definition. Useful when you want your entities pruned to match your
   schematode.

   If you find yourself adding unnecessary things to your schema, you
   should instead define the map by hand."
  [schematode-schema]
  (into {} (for [[k v] schematode-schema] [k (map #(slam k (first %)) (:attrs v))])))

(defn ensure-db-id
  "Ensure an entity has a db/id using tempid."
  ([part entity]
     (if (nil? (:db/id entity))
       (merge {:db/id (d/tempid part)} entity)
       entity))
  ([partitions entity-type entity]
     (ensure-db-id (entity-type partitions) entity)))

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
  [partitions attr-map db-conn entity-type entity]
  (->> entity
       (ensure-db-id partitions entity-type)
       (prune-entity attr-map entity-type)
       (conj [])
       (tx! db-conn)))

(comment

  ;; Callers should def their own schema
  (def schematode-def
    [[:person {:attrs [[:name :string :db.unique/identity]
                       [:favorite-dessert :ref]]}]
     [:dessert {:attrs [[:name :string :db.unique/identity]]
                :part :desserts}]])

  ;; Callers should def their own partition map
  (def partitions {:person :db.part/user
                   :dessert :db.part/desserts})

  ;; Callers should def their own valid-attrs. Attributes not in this
  ;; map will be pruned by tx-clean-entity!
  (def valid-attrs {:person [:person/name :person/favorite-dessert]
                    :dessert [:dessert/name]})

  ;; Alternatively, partition maps and valid-attrs can be generated
  ;; from your Schematode definition. Caution! If you add anything to
  ;; your Schematode that doesn't belong there so you can keep using
  ;; these shortcuts, you should instead make these by hand!

  (def partitions2 (hatch/schematode->partitions schematode-def))

  (def valid-attrs2 (hatch/schematode->attrs schematode-def))

  ;; callers should def their own tx-entity! fns kinda like this
  (def tx-entity! (partial hatch/tx-clean-entity! partitions valid-attrs))
  (def tx-entity2! (partial hatch/tx-clean-entity! partitions2 valid-attrs2))

  ;; galleon will do this init stuff
  (schematode/init-schematode-constraints! (:db-conn ht-config/system))
  (schematode/load-schema! (:db-conn ht-config/system) schematode-def)

  ;; callers can then do stuff like this
  (tx-entity! (:db-conn ht-config/system) :dessert {:dessert/name "ice cream"})
  (tx-entity! (:db-conn ht-config/system)
             :person
             {:person/name "Jon"
              :person/favorite-dessert [:dessert/name "ice cream"]})

  (tx-entity2! (:db-conn ht-config/system) :dessert {:dessert/name "pie"})
  (tx-entity2! (:db-conn ht-config/system)
             :person
             {:person/name "Becky"
              :person/favorite-dessert [:dessert/name "pie"]})

)
