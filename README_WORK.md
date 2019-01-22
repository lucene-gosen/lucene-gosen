```
$ gradle --version

------------------------------------------------------------
Gradle 5.1.1
------------------------------------------------------------

Build time:   2019-01-10 23:05:02 UTC
Revision:     3c9abb645fb83932c44e8610642393ad62116807

Kotlin DSL:   1.1.1
Kotlin:       1.3.11
Groovy:       2.5.4
Ant:          Apache Ant(TM) version 1.9.13 compiled on July 10 2018
JVM:          11.0.2 (Oracle Corporation 11.0.2+7-LTS)
OS:           Mac OS X 10.14.2 x86_64

$ git branch
* master
```

とりあえず`clean`してみた

```
$ gradle clean

FAILURE: Build failed with an exception.

* Where:
Build file '/Users/k-hiraga/Work/src/3rd-party/lucene-gosen/build.gradle' line: 39

* What went wrong:
A problem occurred evaluating root project 'lucene-gosen'.
> Could not find method leftShift() for arguments [build_2j9sc6l3ig5z78g9z6841iphq$_run_closure5@22beab46] on task ':rebuildNaistChasenDic' of type org.gradle.api.DefaultTask.

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 0s
```

失敗したので、ブランチを切って

```
$ git checkout -b gradle_build_fix
Switched to a new branch 'gradle_build_fix'
```

いくつかのファイルを修正

```
$ git status
On branch gradle_build_fix
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   build.gradle
	modified:   dictionary/build.gradle
	modified:   dictionary/ipadic.properties
	modified:   dictionary/naist-chasen.properties
	modified:   gradle.properties
```

エラーは変わったけど、やっぱり失敗...
どうも`javaexec`が動かないみたい。

```
$ gradle clean

> Configure project :
rebuilding dicitonary
rebuilding dicitonary
Download http://jaist.dl.osdn.jp/naist-jdic/31880/naist-jdic-0.4.3.tar.gz
Download http://chasen.naist.jp/stable/ipadic/ipadic-2.6.1.tar.gz

> Configure project :dictionary
Error: Could not find or load main class net.java.sen.tools.DictionaryPreprocessor
Caused by: java.lang.ClassNotFoundException: net.java.sen.tools.DictionaryPreprocessor

FAILURE: Build failed with an exception.

* Where:
Build file '/Users/k-hiraga/Work/src/3rd-party/lucene-gosen/dictionary/build.gradle' line: 111

* What went wrong:
A problem occurred evaluating project ':dictionary'.
> Process 'command '/Library/Java/JavaVirtualMachines/jdk-11.0.2.jdk/Contents/Home/bin/java'' finished with non-zero exit value 1

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 3s
```
