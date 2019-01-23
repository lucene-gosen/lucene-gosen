## Download from Maven Central

* group id : com.github.lucene-gosen
* artifact id : lucene-gosen

There are three types of jar files:

* lucene-gosen-<version>.jar : Only java library, not include dictionary.
* lucene-gosen-<version>-ipadic.jar : Java library with IPA dictionary.
* lucene-gosen-<version>-naist-chasen.jar : Java library with Naist Chasen dictionary

## Installation With Apache Solr 7.4.0:

1. Download jar file from Maven Central Repository
2. Create <your_solr_home>/<collection_dir>/lib and put this jar file in it.
3. Copy stopwords_ja.txt and stoptags_ja.txt into <your_solr_home>/<collection_dir>/conf/lang
4. Add "text_ja_gosen" fieldtype: see example/schema.xml.snippet for example configuration.

Please refer to `example/` for an example japanese configuration with comments explaining
   what the various configuration options are.

## Installation with Apache Lucene 7.4.0:

### Using Maven

Add dependency to pom.xml.

```
    <dependencies>
        <dependency>
            <groupId>com.github.lucene-gosen</groupId>
            <artifactId>lucene-gosen</artifactId>
            <version>7.4.0</version>
            <classifier>ipadic</classifier>
        </dependency>
        ...
    </dependencies>
```

### Non Maven project

1. Download jar file from Maven Central Repository
2. Add this jar file to your classpath, and use GosenAnalyzer, or make your own analyzer from
   the various filters. Its recommended you extend ReusableAnalyzerBase to make any custom analyzer!

## Build

You can build the project using Gradle. And you should use `gradlew` command.

Build only jar file without dictionary

```
$ ./gradlew jar
```

Build jar file with IPA dictionary

```
$ ./gradlew jarWithIpaDic
```

Build jar file with Naist Chasen dictionary

```
$ ./gradlew jarWithNaistChasen
```

Please note that you should modify the following line in `gradle.properties` if you want to build the Gosen for Solr 7.1 or before. 

```
luceneVersion = 7.4.0
```