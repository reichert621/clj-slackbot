(ns slackbot.core
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer [defroutes ANY GET POST PUT DELETE]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]]
            [clojure.java.jdbc :as sql]
            [clj-slack.api :as slack-api]
            [clj-slack.channels :as slack-channels]
            [clj-slack.chat :as slack-chat]
            [clj-http.client :as http]
            [cheshire.core :as json]))

(def pg-db {:dbtype "postgresql"
            :dbname (System/getenv "GIFFALO_DB_NAME")
            :host (System/getenv "GIFFALO_DB_HOST")})

; SQL examples
(defn fetch []
  (sql/query pg-db ["select * from users"]))

(defn create [name]
  (sql/insert! pg-db :users {:name name}))

(def slack-token
  (System/getenv "SLACK_TOKEN"))

(def giphy-token
  (System/getenv "GIPHY_API_KEY"))

(def slack-connection
  {:api-url "https://slack.com/api" :token slack-token})

; TODO: figure out better way to include API key
(def giphy-upload-api
  (str "https://upload.giphy.com/v1/gifs?api_key=" giphy-token))

(defn ping-slack []
  (slack-api/test slack-connection))

(defn get-slack-channels []
  (slack-channels/list slack-connection))

(defn send-slack-msg [channel msg]
  (println "Sending slack message" msg "to" channel)
  (slack-chat/post-message slack-connection channel msg))

(defn pong [req]
  (response {:ok (:ok (ping-slack))}))

(defn handle-slash-command [req]
  (response {:message (-> req :params :text)}))

(defn download-video [url]
  (http/get url {:headers {"Authorization" (str "Bearer " slack-token)}
                 :as :stream}))

(defn upload-giphy [stream]
  (http/post giphy-upload-api
             {:multipart [{:name "file" :content stream}]}))

(defn get-giphy-url [resp]
  (let [body (json/parse-string (:body resp))
        id ((body "data") "id")]
    (str "https://media2.giphy.com/media/" id "/giphy.gif")))

(defn generate-giphy [url]
  (-> url
      (download-video)
      :body
      (upload-giphy)
      (get-giphy-url)))

(defn is-video [file]
  (clojure.string/includes? (-> file :mimetype) "video"))

(defn has-video-file [files]
  (some is-video files))

(defn get-video-urls [files]
  (->> files
       (filter is-video)
       (map :url_private)))

(defn send-giphy-to-slack [channel url]
  (let [giphy (generate-giphy url)]
    (println "Giphy created:" giphy)
    (send-slack-msg channel giphy)))

(defn handle-video-event [channel files]
  (let [url (first (get-video-urls files))]
    (println "Processing url:" url)
    (send-giphy-to-slack channel url)))

(defn handle-slack-event [req]
  (let [challenge (-> req :body :challenge)
        {:keys [channel files]} (-> req :body :event)]
    (println "Event from channel" channel files)
    ; TODO: don't block response, this causes Slack to freak out
    (if (has-video-file files)
      (handle-video-event channel files)
      (println "No video found!"))
    (response challenge)))

(defroutes routes
           (GET "/ping" [] pong)
           (POST "/slash" [] handle-slash-command)
           (POST "/event" [] handle-slack-event))

(defn app [port]
  (jetty/run-jetty
    (-> routes
        wrap-keyword-params
        wrap-params
        wrap-json-response
        (wrap-json-body {:keywords? true}))
    {:port port}))

(defn -main [] (app 8000))