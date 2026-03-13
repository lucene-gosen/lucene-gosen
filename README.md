## Requirements

* Java 11 or later (tested with JDK 25)
* Apache Lucene / Solr 10.x

## Download from Maven Central

* group id : com.github.lucene-gosen
* artifact id : lucene-gosen

There are four types of jar files:

* lucene-gosen-\<version>.jar : Only java library, not include dictionary.
* lucene-gosen-\<version>-ipadic.jar : Java library with IPA dictionary.
* lucene-gosen-\<version>-naist-chasen.jar : Java library with Naist Chasen dictionary.
* lucene-gosen-\<version>-unidic.jar : Java library with UniDic dictionary (compile from source; see below).

## Installation With Apache Solr 10.x:

1. Download jar file from Maven Central Repository
2. Create <your_solr_home>/<collection_dir>/lib and put this jar file in it.
3. Copy stopwords_ja.txt and stoptags_ja.txt into <your_solr_home>/<collection_dir>/conf/lang
4. Add "text_ja_gosen" fieldtype: see example/schema.xml.snippet for example configuration.

Please refer to `example/` for an example japanese configuration with comments explaining
   what the various configuration options are.

## Installation with Apache Lucene 10.x:

### Using Maven

Add dependency to pom.xml.

```
    <dependencies>
        <dependency>
            <groupId>com.github.lucene-gosen</groupId>
            <artifactId>lucene-gosen</artifactId>
            <version>10.4.0</version>
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

Build jar file with UniDic dictionary

The UniDic source archive is downloaded automatically from NINJAL. No manual setup is required:

```
$ ./gradlew jarWithUnidic
```

This automatically downloads `unidic-cwj-202512_full.zip`, unpacks it, runs the
two-phase preprocessing pipeline (`DictionaryTrainer` → `DictionaryCompiler`),
and packages the resulting binary into the jar.

To recalculate word/connection costs from the CRF weights in `model.def` (Phase 2):

```
$ ./gradlew :dictionary:preprocessUnidic -PuseModel=true
$ ./gradlew jarWithUnidic
```

To add custom words to the dictionary with CRF-computed costs, prepare a CSV file
with at least 13 columns per line:

```
surface,leftId,rightId,cost,pos1,pos2,pos3,pos4,cType,cForm,orthBase,pron,pronBase
```

For example:

```
バラク,0,0,0,名詞,固有名詞,人名,名,*,*,バラク,バラク,バラク
オバマ,0,0,0,名詞,固有名詞,人名,姓,*,*,オバマ,オバマ,オバマ
```

The `leftId`, `rightId`, and `cost` columns (0,0,0) are placeholders — when
`-PuseModel=true` is specified, the word cost is recomputed from the CRF model
based on the POS features. Connection costs are resolved at runtime through
POS key matching against the existing UniDic connection cost matrix.

```
$ ./gradlew :dictionary:preprocessUnidic -PuseModel=true -PcustomDic=/path/to/custom.csv
$ ./gradlew jarWithUnidic
```

Multiple custom dictionary files can be specified (space-separated):

```
$ ./gradlew :dictionary:preprocessUnidic -PuseModel=true \
    -PcustomDic="/path/to/names.csv /path/to/places.csv"
```

Please note that you should modify the following line in `gradle.properties` if you want to target a different Lucene version.

```
luceneVersion = 10.4.0
```