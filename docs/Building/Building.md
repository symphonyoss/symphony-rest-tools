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

## Get The Source Code
It is possible to clone the main GitHub repo directly, but if you intend to contribute to the project eventually you will need to fork the repo and raise pull requests for your contributions.

### Clone the Main Repo
If you just want to look around then you can clone the main repo directly with the following commands

```
$ mkdir /some/convenient/directory
$ cd /some/convenient/directory
$ git clone https://github.com/symphonyoss/symphony-rest-tools.git
Cloning into 'symphony-rest-tools'...
remote: Counting objects: 1955, done.
remote: Compressing objects: 100% (70/70), done.
remote: Total 1955 (delta 59), reused 65 (delta 25), pack-reused 1854
Receiving objects: 100% (1955/1955), 3.23 MiB | 411.00 KiB/s, done.
Resolving deltas: 100% (1045/1045), done.
$ cd symphony-rest-tools/
$ ls
CONTRIBUTING.md                                         pom.xml
LICENSE                                                 symphony-rest-tools-bundles
NOTICE                                                  symphony-rest-tools-features
README.md                                               symphony-rest-tools-products
docs                                                    symphony-rest-tools-update
$ 
```

### Forking the Repo
If you want to be able to contribute to the project then you can fork the repo.
In your web browser, navigate to https://github.com/symphonyoss/symphony-rest-tools and click the 
**Fork** button in the top right hand corner.

![Fork the Repo](./forkRepo.jpg)

Now you can clone your fork with the following commands, replace **yourname** with your GitHub user name:

```
$ mkdir /some/convenient/directory
$ cd /some/convenient/directory
$ git clone https://github.com/yourname/symphony-rest-tools.git
Cloning into 'symphony-rest-tools'...
remote: Counting objects: 1955, done.
remote: Compressing objects: 100% (70/70), done.
remote: Total 1955 (delta 59), reused 65 (delta 25), pack-reused 1854
Receiving objects: 100% (1955/1955), 3.23 MiB | 411.00 KiB/s, done.
Resolving deltas: 100% (1045/1045), done.
$ cd symphony-rest-tools/
$ ls
CONTRIBUTING.md                                         pom.xml
LICENSE                                                 symphony-rest-tools-bundles
NOTICE                                                  symphony-rest-tools-features
README.md                                               symphony-rest-tools-products
docs                                                    symphony-rest-tools-update
$ git remote add upstream https://github.com/symphonyoss/symphony-rest-tools.git
$ git remote -v
origin  https://github.com/yourname/symphony-rest-tools.git (fetch)
origin  https://github.com/yourname/symphony-rest-tools.git (push)
upstream        https://github.com/symphonyoss/symphony-rest-tools.git (fetch)
upstream        https://github.com/symphonyoss/symphony-rest-tools.git (push)
$ 

```

Creating the **upstream** remote will allow you to raise a pull request when you are ready to make a contribution.