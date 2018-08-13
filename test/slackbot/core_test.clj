(ns slackbot.core-test
  (:require [clojure.test :refer :all]
            [slackbot.core :refer :all]))

(deftest slack-token-test
  (testing "should not be nil"
    (is (not (nil? slack-token)))))

(deftest giphy-token-test
  (testing "should not be nil"
    (is (not (nil? giphy-token)))))

(deftest is-video-test
  (testing "should be true if mimetype is video"
    (is (= true (is-video {:mimetype "video"}))))
  (testing "should be true if mimetype is video/mov"
    (is (= true (is-video {:mimetype "video/mov"}))))
  (testing "should be false if mimetype is image"
    (is (= false (is-video {:mimetype "image"})))))

(deftest has-video-file-test
  (testing "should be true if video file exists"
    (is (= true (has-video-file [{:mimetype "video"}
                                 {:mimetype "image"}]))))
  (testing "should be false if no video file exists"
    (is (= nil (has-video-file [{:mimetype "text"}
                                 {:mimetype "image"}])))))

(deftest get-video-urls-test
  (testing "should extract video urls"
    (is (= ["http://v1.com" "http://v2.com"]
           (get-video-urls [{:mimetype "video" :url_private "http://v1.com"}
                            {:mimetype "video" :url_private "http://v2.com"}
                            {:mimetype "image" :url_private "http://img.com"}])))))

