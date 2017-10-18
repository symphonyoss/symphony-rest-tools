# Building
This project uses maven as its build manager.
## Quick Start
Create a suitable directory for your working version 
### Building

```
$ git clone https://github.com/bruceskingle/symphony-rest-tools.git
Cloning into 'symphony-rest-tools'...
remote: Counting objects: 5, done.
remote: Compressing objects: 100% (5/5), done.
remote: Total 5 (delta 0), reused 0 (delta 0), pack-reused 0
Unpacking objects: 100% (5/5), done.
$ cd symphony-rest-tools
$ mvn package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Symphony REST tools 0.1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.639 s
[INFO] Finished at: 2017-06-19T19:37:31-07:00
[INFO] Final Memory: 27M/331M
[INFO] ------------------------------------------------------------------------
$ tree target/symphony-rest-tools-0.1.0-SNAPSHOT-bin
target/symphony-rest-tools-0.1.0-SNAPSHOT-bin
├── bin
│   ├── environment.sh
│   └── probePod.sh
└── lib
    ├── jcurl-0.9.4-SNAPSHOT.jar
    ├── jsr305-3.0.2.jar
    └── symphony-rest-tools-0.1.0-SNAPSHOT.jar

2 directories, 5 files
$ ./target/symphony-rest-tools-0.1.0-SNAPSHOT-bin/bin/probePod.sh -keystore /tmp/bot.user1.p12 nexus.symphony.com

```

## Eclipse, Maven and Tycho
**symphony-rest-tools** delivers both a pure Java command line tool set and library distributed via **Maven**, and an **Eclipse E4** based GUI for Windows, Mac, and Linux computers.

The Eclipse eco-system uses OSGi for component and dependency management, which is different to and separate from, the Maven dependency management system. In order to make the build work for the OSGi based GUIs, this project uses OSGi dependency management (a.k.a. "Manifest-First" dependencies) for it's build.

The Maven Tycho plugins allow for "Manifest-First" development with Maven build automation, and despite using "Manifest-First" dependency management, this project is built and released with the usual Maven commands.

In order to allow pure java consumers to access the non-UI library through the familiar Maven dependency mechanism we also maintain "Pom-First" dependency information, which is redundant as far as the build for this project is concerned.

All of this leads to one or two "weird things" and this is why, although the project is developed fully using Eclipse, that if you simply clone the GitHub repo and import the projects into Eclipse you will see build errors. We have tried hard to ensure that none of this trips up users who just want to get the source downloaded and compiled, or to use the binary distributions. The quick start examples show you a set of commands which will get an environment up and running in a few minutes. For these reasons we **strongly advise** you to try following the examples _exactly as written_ in the first instance.