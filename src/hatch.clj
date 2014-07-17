(ns hatch
  ^{:author "David Zaharee <dzaharee@vlacs.org>"
    :doc "Useful functions for working with Datomic and Schematode"}
  (:require [datomic.api :as d]
            [datomic-schematode :as schematode]))

(defn slam
  "Slams two keywords together into one namespaced keyword"
  [ns n]
  (keyword (name ns) (name n)))

(defn slam-in
  "Slams a namespace on a key in a map"
  [m ns k]
  (if (contains? m k)
      (-> m
          (assoc (slam ns k) (k m))
          (dissoc k))
      m))

(defn slam-all
  "Slam a namespace on all keys in a map.
   Generally use namespaced attributes in your code.

   slam-all is there for you when you have non-namespaced
   attributes, such as when you get data from an external system."
  [m ns]
  (into {} (for [[k v] m] [(slam ns k) v])))

(defn schematode->partitions
  "Make a hatch partition map from your schematode definition.

   If you find yourself adding unnecessary things to your schema, you
   should instead define the map by hand."
  [schematode-schema]
  (into {}
        (map
          (fn [m]
            [(:namespace m) (slam :db.part (or (:part m) :user))])
          schematode-schema)))

(defn schematode->attrs
  "Make a hatch attribute pruning map from your schematode
  definition. Useful when you want your entities pruned to match your
  schematode.

   If you find yourself adding unnecessary things to your schema, you
   should instead define the map by hand."
  [schematode-schema]
  (into {}
        (map
         (fn [i]
           [(:namespace i) (map #(slam (:namespace i) (first %)) (:attrs i []))])
         schematode-schema)))

(defn ensure-db-id
  "Ensure an entity has a db/id using tempid."
  ([part entity]
     (if (nil? (:db/id entity))
       (merge {:db/id (d/tempid part)} entity)
       entity)))

(defn prune-entity
  "Prune an entity using attr list."
  [attrs entity]
  (->> attrs
       (map #(if (sequential? %1)
               (first %1)
               %1))
       (cons :db/id)
       (select-keys entity)))

(defn merge-composites
  "Merge together composite keys"
  [composites entity]
  (->> entity
       (repeat)
       (map #(%1 %2) composites)
       (apply str)))

(defn ensure-composite-keys
  "Ensure an entity's composite key(s) are set"
  [attrs entity]
  (apply merge
         entity
         (for [k attrs
               :when (sequential? k)]
           {(first k) (merge-composites (rest k) entity)})))

(defn clean-entity
  "Ensure entity has a db/id and prune it"
  [partitions attr-map entity-type entity]
  (let [attrs (entity-type attr-map)]
    (->> entity
         (ensure-db-id (entity-type partitions))
         (ensure-composite-keys attrs)
         (prune-entity attrs))))

(defn tx!
  "Transact with schematode constraints"
  [db-conn txs]
  (schematode/tx db-conn :enforce txs))

(defn tx-clean-entity!
  "Clean up an entity (ensure it has a db/id and prune it), then transact it."
  [partitions attr-map db-conn entity-type entity]
  (let [cleaned-entity (clean-entity partitions attr-map entity-type entity)]
    (if (empty? (dissoc cleaned-entity :db/id))
      (throw (Exception. "cannot transact empty entity"))
      (tx! db-conn [cleaned-entity]))))

(comment

  ;; If you're in hatch, open a repl and eval
  (reset)
  ;; to work through these examples

  ;; "(:db-conn ht-config/system)" is a datomic connection

  ;; Callers should def their own schema
  (def schematode-def
    [{:namespace :person
      :attrs [[:id-sk :string]
              [:id-sk-origin :keyword]
              [:id-sk-with-origin :string :db.unique/identity]
              [:name :string]
              [:favorite-dessert :ref]]}
     {:namespace :dessert
      :attrs [[:name :string :db.unique/identity]]
      :part :desserts}])

  ;; Callers should def their own partition map
  (def partitions {:person :db.part/user
                   :dessert :db.part/desserts})

  ;; Callers should def their own attribute map. Attributes not in this
  ;; map will be pruned by tx-clean-entity! To have hatch smush together
  ;; composite attributes for upserting, define them as a vector in the
  ;; form [attribute & attribute-composites]
  (def attrs {:person [:person/id-sk
                       :person/id-sk-origin
                       [:person/id-sk-with-origin :person/id-sk :person/id-sk-origin]
                       :person/name
                       :person/favorite-dessert]
              :dessert [:dessert/name]})

  ;; Alternatively, partition and attribute maps can be generated
  ;; from your Schematode definition. Caution! Don't add anything to
  ;; your Schematode definition that doesn't belong there! If you are
  ;; tempted to do so, you should instead make these by hand!

  (def partitions2 (hatch/schematode->partitions schematode-def))

  (def attrs2 (hatch/schematode->attrs schematode-def))

  ;; Callers should def their own tx-entity! fns kinda like this
  (def tx-entity! (partial hatch/tx-clean-entity! partitions attrs))
  (def tx-entity2! (partial hatch/tx-clean-entity! partitions2 attrs2))

  ;; Galleon will do this stuff
  (schematode/init-schematode-constraints! (:db-conn ht-config/system))
  (schematode/load-schema! (:db-conn ht-config/system) schematode-def)

  ;; Callers can then do stuff like this
  (tx-entity! (:db-conn ht-config/system) :dessert {:dessert/name "ice cream"})
  (tx-entity! (:db-conn ht-config/system)
             :person
             {:person/id-sk "1"
              :person/id-sk-origin :internal
              :person/name "Jon"
              :person/favorite-dessert [:dessert/name "ice cream"]})

  (tx-entity2! (:db-conn ht-config/system) :dessert {:dessert/name "pie"})
  (tx-entity2! (:db-conn ht-config/system)
             :person
             {:person/id-sk "2"
              :person/id-sk-origin :internal
              :person/name "Becky"
              :person/favorite-dessert [:dessert/name "pie"]})

  (ptouch-that '[:find ?e :where [?e :person/name]])
  (ptouch-that '[:find ?e :where [?e :dessert/name]])

)
