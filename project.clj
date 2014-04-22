(defproject org.vlacs/hatch "0.1.2"
  :description "Hatch, how we get things in and out of the hold (Datomic)."
  :url "https://github.com/vlacs/hatch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.datomic/datomic-free "0.9.4707"]
                 [datomic-schematode "0.1.0-RC1"]]
  :pedantic? :warn ; :abort
  :plugins [[lein-cloverage "1.0.2"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]]}})
