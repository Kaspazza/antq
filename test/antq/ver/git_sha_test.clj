(ns antq.ver.git-sha-test
  (:require
   [antq.util.git :as u.git]
   [antq.ver :as ver]
   [antq.ver.git-sha]
   [clojure.test :as t]))

(t/deftest get-sorted-versions-test
  (with-redefs [u.git/head-sha-by-ls-remote (fn [url]
                                              (when (= url "https://example.com")
                                                "head-sha"))]
    (t/is (= ["head-sha"]
             (ver/get-sorted-versions {:type :git-sha :extra {:url "https://example.com"}})))

    (t/is (= []
             (ver/get-sorted-versions {:type :git-sha :extra {:url "failed to fetch"}})))))

(t/deftest latest?-test
  (t/is (true? (ver/latest? {:type :git-sha :version "foo" :latest-version "foo"})))
  (t/is (false? (ver/latest? {:type :git-sha :version "foo" :latest-version "bar"})))

  (t/testing "comparing short SHA and long SHA"
    (t/is (true? (ver/latest? {:type :git-sha
                               :version "8be0919"
                               :latest-version "8be09192b01d78912b03852f5d6141e8c48f4179"})))))

