(ns antq.upgrade.boot-test
  (:require
   [antq.dep.boot :as dep.boot]
   [antq.record :as r]
   [antq.test-helper :as h]
   [antq.upgrade :as upgrade]
   [antq.upgrade.boot]
   [clojure.java.io :as io]
   [clojure.test :as t]))

(def ^:private dummy-java-dep
  (r/map->Dependency {:project :boot
                      :type :java
                      :name "bar/bar"
                      :latest-version "9.0.0"
                      :file (io/resource "dep/build.boot")}))

(t/deftest upgrade-dep-test
  (let [from-deps (->> dummy-java-dep
                       :file
                       (slurp)
                       (dep.boot/extract-deps ""))
        to-deps (->> dummy-java-dep
                     (upgrade/upgrader)
                     (dep.boot/extract-deps ""))]
    (t/is (= #{{:name "bar/bar" :version {:- "2.0.0" :+ "9.0.0"}}}
             (h/diff-deps from-deps to-deps)))))
