(defproject org.vlacs/hatch "0.2.3"
  :description "Hatch, how we get things in and out of the hold (Datomic)."
  :url "https://github.com/vlacs/hatch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.datomic/datomic-free "0.9.4766.11"]
                 ^{:voom {:repo "https://github.com/vlacs/datomic-schematode" :branch "dev"}}
                 [datomic-schematode "0.1.3-RC1-20140624_195331-g210f6fa"]]
  :pedantic? :warn ; :abort
  :plugins [[lein-cloverage "1.0.2"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [org.clojure/test.check "0.5.7"]
                                  [com.velisco/herbert "0.6.1"]]}})
