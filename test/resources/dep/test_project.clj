(defproject foo "0.1.0-SNAPSHOT"
  :dependencies [[foo/core "1.0.0"]
                 ^:inline-dep [bar "2.0.0" :exclusions [org.clojure/clojure]]
                 [ver-not-string :version]
                 [ver-empty ""]
                 ;; should be ignored
                 ^:antq/exclude [meta-ignore "3.0.0"]]

  :plugins [[plug "4.0.0"]]

  :profiles
  {:foo
   {:dependencies [[baz "3.0.0"]]}

   :same-name
   [;; same artifact name, but different version number
    :dependencies [[foo/core "1.1.0"]]]

   :same-name-but-excluded
   [;; same artifact name, but excluded
    :dependencies [^:antq/exclude [foo/core "0.0.1"]]]}

  :repositories [["antq-test" {:url "s3://antq-repo/"}]
                 ["str-test" "https://example.com"]]

  :managed-dependencies [[managed/dependency "5.0.0"]])
