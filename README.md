## Download from Maven Central

* group id : com.github.lucene-gosen
* artifact id : lucene-gosen

We upload three types jar file

* lucene-gosen-<version>.jar : Only java library, not include dictionary.
* lucene-gosen-<version>-ipadic.jar : Java library with IPA dictionary.
* lucene-gosen-<version>-naist-chasen.jar : Java library with Naist Chasen dictionary

## Installation With Apache Solr 6.0.1:

1. Download jar file from Maven Central Repository
2. create <your_solr_home>/<collection_dir>/lib and put this jar file in it.
3. copy stopwords_ja.txt and stoptags_ja.txt into <your_solr_home>/<collection_dir>/conf/lang
4. add "text_ja_gosen" fieldtype: see example/schema.xml.snippet for example configuration.

refer to example/ for an example japanese configuration with comments explaining
   what the various configuration options are.

## Installation with Apache Lucene 6.0.1:

### Using Maven

Add dependency to pom.xml.

```
    <dependencies>
        <dependency>
            <groupId>com.github.lucene-gosen</groupId>
            <artifactId>lucene-gosen</artifactId>
            <version>6.0.1</version>
            <classifier>ipadic</classifier>
        </dependency>
        ...
    </dependencies>
```

### Non Maven project

1. Download jar file from Maven Central Repository
2. add this jar file to your classpath, and use GosenAnalyzer, or make your own analyzer from
   the various filters. Its recommended you extend ReusableAnalyzerBase to make any custom analyzer!

## Build

You can build the project using Gradle.

Build only jar file without dictionary

```
$ gradle jar
```

Build jar file with IPA dictionary

```
$ gradle jarWithIpaDic
```

Build jar file with Naist Chasen dictionary

```
$ gradle jarWithNaistChasen
```

