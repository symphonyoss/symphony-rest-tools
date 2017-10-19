---
nav-sort: 500
---
# Building
This project uses maven as its build manager.

## Eclipse, Maven and Tycho
**symphony-rest-tools** delivers both a pure Java command line tool set and library distributed via **Maven**, and an **Eclipse E4** based GUI for Windows, Mac, and Linux computers.

The Eclipse eco-system uses OSGi for component and dependency management, which is different to and separate from, the Maven dependency management system. In order to make the build work for the OSGi based GUIs, this project uses OSGi dependency management (a.k.a. "Manifest-First" dependencies) for it's build.

The Maven Tycho plugins allow for "Manifest-First" development with Maven build automation, and despite using "Manifest-First" dependency management, this project is built and released with the usual Maven commands.

In order to allow pure java consumers to access the non-UI library through the familiar Maven dependency mechanism we also maintain "Pom-First" dependency information, which is redundant as far as the build for this project is concerned.

All of this leads to one or two "weird things" and this is why, although the project is developed fully using Eclipse, that if you simply clone the GitHub repo and import the projects into Eclipse you will see build errors. We have tried hard to ensure that none of this trips up users who just want to get the source downloaded and compiled, or to use the binary distributions. The quick start examples show you a set of commands which will get an environment up and running in a few minutes. For these reasons we **strongly advise** you to try following the examples _exactly as written_ in the first instance.
