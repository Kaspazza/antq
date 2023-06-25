(ns antq.util.maven-test
  (:require
   [antq.util.env :as u.env]
   [antq.util.maven :as sut]
   [clojure.data.xml :as xml]
   [clojure.java.io :as io]
   [clojure.test :as t]
   [clojure.tools.deps.util.maven :as deps.util.maven])
  (:import
   java.util.UUID
   org.apache.maven.model.DistributionManagement
   (org.apache.maven.settings
    Server
    Settings)))

(def ^:private dummy-settings
  (doto (Settings.)
    (.addServer (doto (Server.)
                  (.setId "serv1")))
    (.addServer (doto (Server.)
                  (.setId "serv2")
                  (.setUsername "two-user")
                  (.setPassword "two-pass")))))

(def ^:private dummy-repos
  {;; duplicated with dummy-settings
   "serv1" {:url "https://one.example.com"}
   ;; duplicated with dummy-settings
   "serv2" {:url "https://two.example.com"}
   ;; new to appear
   "serv3" {:url "https://three.example.com"
            :username "three-user"
            :password "three-pass"}
   ;; new to appear
   "serv4" {:url "https://three.example.com"
            :username :env
            :password :env/four}
   ;; should not be added because of missing username and password
   "dummy" {:url "https://dummy.example.com"}})

(def ^:private dummy-env
  {"LEIN_PASSWORD" "lein-pass"
   "FOUR" "env-four"})

(def ^:private test-pom-path
  (.getAbsolutePath (io/file (io/resource "util/maven/pom.xml"))))

(t/deftest normalize-repo-url-test
  (t/are [expected in] (= expected (sut/normalize-repo-url in))
    "" ""
    "foo" "foo"
    "s3://foo/bar" "s3p://foo/bar"))

(t/deftest normalize-repos-test
  (t/is (= sut/default-repos
           (sut/normalize-repos sut/default-repos)))
  (t/is (= {"foo" {:url "s3://bar"}}
           (sut/normalize-repos {"foo" {:url "s3://bar"}})))
  (t/is (= {"foo" {:invalid "invalid"}}
           (sut/normalize-repos {"foo" {:invalid "invalid"}})))

  (t/testing "replace s3p:// to s3://"
    (t/is (= {"foo" {:url "s3://bar"}}
             (sut/normalize-repos {"foo" {:url "s3p://bar"}})))
    (t/is (= {"foo" {:url "s3://bar" :no-auth true}}
             (sut/normalize-repos {"foo" {:url "s3p://bar" :no-auth true}})))))

(t/deftest snapshot?-test
  (t/are [expected in] (= expected (sut/snapshot? in))
    false ""
    false "foo"
    true "foo-snapshot"
    true "foo-SnapShot"
    true "foo-SNAPSHOT"))


(t/deftest get-maven-settings-test
  (with-redefs [deps.util.maven/get-settings (constantly dummy-settings)
                u.env/getenv #(get dummy-env %)]
    (let [settings (sut/get-maven-settings {:repositories dummy-repos})
          servers (map #(hash-map
                         :id (.getId %)
                         :username (.getUsername %)
                         :password (.getPassword %))
                       (.getServers settings))]
      (t/is (= 4 (count servers)))

      (t/is (= #{{:id "serv1" :username nil :password nil}
                 ;; from settings.xml
                 {:id "serv2" :username "two-user" :password "two-pass"}
                 ;; from project.clj
                 {:id "serv3" :username "three-user" :password "three-pass"}
                 ;; from project.clj with environmental variable
                 {:id "serv4" :username "lein-pass" :password "env-four"}}
               (set servers))))))

(t/deftest read-pom-s3-repos-test
  (t/is (nil? (sut/read-pom "s3://foo"))))

(t/deftest get-url-test
  (let [model (sut/read-pom test-pom-path)]
    (t/is (= "https://github.com/liquidz/antq"
             (sut/get-model-url model)))))

(t/deftest get-scm-url-test
  (let [model (sut/read-pom test-pom-path)
        scm (sut/get-model-scm model)]
    (t/is (= "https://github.com/liquidz/antq"
             (sut/get-scm-url scm)))))

(t/deftest get-distribution-management-test
  (let [model (sut/read-pom test-pom-path)]
    (t/is (instance? DistributionManagement (sut/get-distribution-management model)))))

(t/deftest get-relocation-test
  (let [model (sut/read-pom test-pom-path)
        dm (sut/get-distribution-management model)]
    (t/is (= {:group-id "io.github.liquidz"
              :artifact-id "antq?"
              :message "dummy message"}
             (sut/get-relocation dm)))))

(t/deftest get-local-versions-test
  (let [dummy-file (io/file (io/resource "util/maven/maven-metadata-local.xml"))
        non-existing-file (io/file "/tmp" (str (UUID/randomUUID)))]
    (t/testing "valid maven-metadata-local.xml"
      (with-redefs [io/file (constantly dummy-file)]
        (t/is (= ["8.0.0" "9.0.0"]
                 (#'sut/get-local-versions* 'com.github.liquidz/antq)))))

    (t/testing "non-existing file"
      (with-redefs [io/file (constantly non-existing-file)]
        (t/is (nil? (#'sut/get-local-versions* 'com.github.liquidz/antq)))))

    (t/testing "non-existing file"
      (with-redefs [io/file (constantly dummy-file)
                    xml/parse-str (fn [& _] (throw (ex-info "test" {})))]
        (t/is (nil? (#'sut/get-local-versions* 'com.github.liquidz/antq)))))))
