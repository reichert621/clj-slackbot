(defproject slackbot "0.1.0-SNAPSHOT"
  :description "Clojure Slackbot"
  :url "https://github.com/reichert621/clj-slackbot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repl-options {:port 4001}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.7.0-RC1"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.6.1"]
                 [org.clojure/java.jdbc "0.7.7"]
                 [org.postgresql/postgresql "42.2.4"]
                 [clj-http "3.9.0"]
                 [cheshire "5.8.0"]
                 [org.julienxx/clj-slack "0.5.6"]]
  :min-lein-version "2.8.0"
  :main slackbot.core)
