(defproject jacket "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :source-paths ["src/" "cljs/"]
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.reader "0.7.4"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [compojure "1.1.5"]
                 [domina "1.0.3-SNAPSHOT"]
                 ]

  :plugins [[lein-cljsbuild "0.3.4"]
            [lein-ring "0.8.7"]]

  :ring {:handler jacket.core/handler}

  :cljsbuild {:builds
              [{:source-paths ["cljs"]
                
                :compiler {:output-to "resources/public/js/jacket.js"
                           :optimizations :whitespace
                           :pretty-pring true}
                }]}
  
  :main jacket.core)
