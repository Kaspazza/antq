(ns antq.report.format-test
  (:require
   [antq.report :as report]
   [antq.report.format]
   [antq.test-helper :as h]
   [clojure.string :as str]
   [clojure.test :as t]))

(defn- reporter
  [deps error-format]
  (report/reporter deps {:reporter "format"
                         :error-format error-format}))

(t/deftest reporter-test
  (let [dummy-deps [(h/test-dep {:file "a" :name "foo" :version "1" :latest-version "2"})
                    (h/test-dep {:file "b" :name "bar" :version "1" :latest-version nil})
                    (h/test-dep {:file "c" :name "baz" :version "1" :latest-version "3"
                                 :changes-url "https://example.com"})
                    (h/test-dep {:file "d" :name "old" :version "1" :latest-version nil
                                 :latest-name "new"})]]

    (t/is (seq (with-out-str (reporter
                              dummy-deps
                              "::error file={{file}}::{{message}}"))))

    (t/is (= ["::error file=a::foo,1,2,. "
              "::error file=b::bar,1,Failed to fetch,. "
              "::error file=c::baz,1,3,. https://example.com"
              "::error file=d::old,1,Failed to fetch,new. "]
             (str/split-lines
              (with-out-str
                (reporter
                 dummy-deps
                 "::error file={{file}}::{{name}},{{version}},{{latest-version}},{{latest-name}}. {{changes-url}}")))))

    (t/testing "backward compatibility"
      (t/is (= "https://example.com"
               (str/trim
                (with-out-str
                  (reporter dummy-deps "{{diff-url}}"))))))))
