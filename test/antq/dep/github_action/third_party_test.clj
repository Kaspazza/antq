(ns antq.dep.github-action.third-party-test
  (:require
   [antq.constant.github-action :as const.gh-action]
   [antq.dep.github-action.third-party :as sut]
   [antq.record :as r]
   [clojure.test :as t]))

(defn- git-tag-dependency
  [m]
  (r/map->Dependency (assoc m :type :github-tag)))

(defn- java-dependency
  [m]
  (r/map->Dependency (assoc m :type :java)))

(t/deftest detect-invalid
  (t/is (nil? (sut/detect nil)))
  (t/is (nil? (sut/detect {:uses "foo/bar"}))))

(t/deftest detect-setup-clojure-test
  (t/testing "Clojure Tools"
    (t/is (= [(git-tag-dependency {:name "clojure/brew-install"
                                   :version "1.0.0"
                                   :extra {const.gh-action/type-key "DeLaGuardo/setup-clojure"}})]
             (sut/detect {:uses "DeLaGuardo/setup-clojure@main"
                          :with {:tools-deps "1.0.0"}})))
    (t/is (= [(git-tag-dependency {:name "clojure/brew-install"
                                   :version "2.0.0"
                                   :extra {const.gh-action/type-key "DeLaGuardo/setup-clojure"}})]
             (sut/detect {:uses "DeLaGuardo/setup-clojure@main"
                          :with {:cli "2.0.0"}}))))

  (t/testing "Leiningen"
    (t/is (= [(git-tag-dependency {:name "technomancy/leiningen"
                                   :version "3.0.0"
                                   :extra {const.gh-action/type-key "DeLaGuardo/setup-clojure"}})]
             (sut/detect {:uses "DeLaGuardo/setup-clojure@main"
                          :with {:lein "3.0.0"}}))))

  (t/testing "Boot"
    (t/is (= [(git-tag-dependency {:name "boot-clj/boot"
                                   :version "4.0.0"
                                   :extra {const.gh-action/type-key "DeLaGuardo/setup-clojure"}})]
             (sut/detect {:uses "DeLaGuardo/setup-clojure@main"
                          :with {:boot "4.0.0"}}))))

  (t/testing "Multiple"
    (t/is (= [(git-tag-dependency {:name "clojure/brew-install"
                                   :version "5.0.0"
                                   :extra {const.gh-action/type-key "DeLaGuardo/setup-clojure"}})
              (git-tag-dependency {:name "technomancy/leiningen"
                                   :version "6.0.0"
                                   :extra {const.gh-action/type-key "DeLaGuardo/setup-clojure"}})]
             (sut/detect {:uses "DeLaGuardo/setup-clojure@main"
                          :with {:cli "5.0.0"
                                 :lein "6.0.0"}})))))

(t/deftest detect-setup-clj-kondo-test
  (t/is (= [(java-dependency {:name "clj-kondo/clj-kondo"
                              :version "1.0.0"
                              :extra {const.gh-action/type-key "DeLaGuardo/setup-clj-kondo"}})]
           (sut/detect {:uses "DeLaGuardo/setup-clj-kondo@main"
                        :with {:version "1.0.0"}}))))

(t/deftest detect-setup-graalvm-test
  (t/testing "before v4.0"
    (t/is (= [(git-tag-dependency {:name "graalvm/graalvm-ce-builds"
                                   :version "19.3.0"
                                   :extra {const.gh-action/type-key "DeLaGuardo/setup-graalvm"}})]
             (sut/detect {:uses "DeLaGuardo/setup-graalvm@main"
                          :with {:graalvm-version "19.3.0.java8"}}))))
  (t/testing "v4.0 or later"
    (t/is (= [(git-tag-dependency {:name "graalvm/graalvm-ce-builds"
                                   :version "19.3.0"
                                   :extra {const.gh-action/type-key "DeLaGuardo/setup-graalvm"}})]
             (sut/detect {:uses "DeLaGuardo/setup-graalvm@main"
                          :with {:graalvm "19.3.0.java8"}})))))

(t/deftest detect-setup-cljstyle-test
  (t/is (= [(git-tag-dependency {:name "greglook/cljstyle"
                                 :version "1.0.0"
                                 :extra {const.gh-action/type-key "0918nobita/setup-cljstyle"}})]
           (sut/detect {:uses "0918nobita/setup-cljstyle@main"
                        :with {:cljstyle-version "1.0.0"}}))))
