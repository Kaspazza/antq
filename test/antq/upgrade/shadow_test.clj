(ns antq.upgrade.shadow-test
  (:require
   [antq.dep.shadow :as dep.shadow]
   [antq.record :as r]
   [antq.test-helper :as h]
   [antq.upgrade :as upgrade]
   [antq.upgrade.shadow]
   [clojure.java.io :as io]
   [clojure.test :as t]))

(def ^:private dummy-java-dep
  (r/map->Dependency {:project :shadow-cljs
                      :type :java
                      :name "foo/core"
                      :latest-version "9.0.0"
                      :file (io/resource "dep/test_shadow-cljs.edn")}))

(def ^:private dummy-meta-dep
  (r/map->Dependency {:project :shadow-cljs
                      :type :java
                      :name "with/meta"
                      :latest-version "9.0.0"
                      :file (io/resource "dep/test_shadow-cljs.edn")}))

(t/deftest upgrade-dep-test
  (let [from-deps (->> dummy-java-dep
                       :file
                       (slurp)
                       (dep.shadow/extract-deps ""))
        to-deps (->> dummy-java-dep
                     (upgrade/upgrader)
                     (dep.shadow/extract-deps ""))]
    (t/is (= #{{:name "foo/core" :version {:- "1.0.0" :+ "9.0.0"}}}
             (h/diff-deps from-deps to-deps)))))

(t/deftest upgrade-meta-dep-test
  (let [from-deps (->> dummy-meta-dep
                       :file
                       (slurp)
                       (dep.shadow/extract-deps ""))
        to-deps (->> dummy-meta-dep
                     (upgrade/upgrader)
                     (dep.shadow/extract-deps ""))]
    (t/is (= #{{:name "with/meta" :version {:- "4.0.0" :+ "9.0.0"}}}
             (h/diff-deps from-deps to-deps)))))
