Installation With Apache Solr 4.0:

1. run 'ant'. this will make lucene-gosen-{version}.jar
2. create example/solr/lib and put this jar file in it.
3. copy stopwords_ja.txt and stoptags_ja.txt into example/solr/conf
4. add "text_ja_gosen" fieldtype: see example/schema.xml.snippet for example configuration.

refer to example/ for an example japanese configuration with comments explaining
   what the various configuration options are.

Installation with Apache Lucene 4.0:

1. run 'ant'. this will make lucene-gosen-{version}.jar
2. add this jar file to your classpath, and use GosenAnalyzer, or make your own analyzer from
   the various filters. Its recommended you extend ReusableAnalyzerBase to make any custom analyzer!
