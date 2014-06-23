(defproject org.vlacs/hatch "0.2.0"
  :description "Hatch, how we get things in and out of the hold (Datomic)."
  :url "https://github.com/vlacs/hatch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;;[com.datomic/datomic-free "0.9.4766.11"]
                 [com.datomic/datomic-pro "0.9.4766.11"]
                 [datomic-schematode "0.1.0-RC3"]]
  :pedantic? :warn ; :abort
  :plugins [[lein-cloverage "1.0.2"]]
  :repositories [["my.datomic.com" {:url "https://my.datomic.com/repo"
                                    :username :env/lein_datomic_repo_username
                                    :password :env/lein_datomic_repo_password}]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [org.clojure/test.check "0.5.7"]
                                  [com.velisco/herbert "0.6.1"]]}})
