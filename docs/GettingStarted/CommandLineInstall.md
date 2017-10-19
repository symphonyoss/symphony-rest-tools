---
nav-sort: 110
nav-level: 1
---
# Installing the Command Line Binary Release
The command line binary release is a pure Java, platform neutral, command line UI. It requires a Java 8 Runtime to be installed.

## Download
All Symohony REST Tools releases are available from the releases page at https://github.com/symphonyoss/symphony-rest-tools/releases.

The command line UI is called **symphony-rest-tools-cmdline-X.Y.Z-bin.tar.gz**

This is the pure Java, platform neutral, command line implementation. Note that the command line scripts are also included in each of the UI distributions as well, so you may prefer to install one of them instead.

## Install
There is no installation process as such, simply extract the tarball in a convenient location. Once you do that you should see a file structure similar to this

```
$ tree
.
├── bash
│   ├── checkCerts
│   ├── checkPod
│   ├── environment.sh
│   ├── jcurl
│   └── probePod
├── bat
│   ├── checkCerts.bat
│   ├── checkPod.bat
│   ├── environment.bat
│   ├── jcurl.bat
│   └── probePod.bat
├── certs
│   └── test
│       ├── 127.0.0.1.p12
│       ├── bot.user1-no-root.p12
│       ├── bot.user1.p12
│       ├── bot.user10.p12
```
many similar lines not shown
```
│       ├── server.keystore
│       ├── server.truststore
│       ├── server.truststore.2
│       ├── server.truststore.3
│       ├── wildcard.keystore
│       └── wildcard.symphony.com.p12
└── lib
    ├── bcpkix-jdk15on-1.51.jar
    ├── bcprov-jdk15on-1.51.jar
    ├── jackson-annotations-2.8.7.jar
    ├── jackson-core-2.8.7.jar
    ├── jackson-databind-2.8.7.jar
    ├── jcurl-0.9.7.jar
    ├── jsr305-3.0.2.jar
    └── org.symphonyoss.symphony.tools.rest-0.1.6.jar

5 directories, 85 files
```

The **bash** directory contains shell scripts for Unix based systems, the **bat** directory contains Windows batch file scripts which do the same thing for that platform.

## Run
The launch scripts pass through command line parameters so you can simply type the name of the script followed by additional parameters to run the various commands.

### Switches
Switches are single letter parameters introduced by a single hyphen. Switches can be concatenated in a single command line argument and some of them can be specified multiple times so the following would all be valid:

```
$ someCommand -i -v
$ someCommand -iv
$ someCommand -iivvv
```

Note that most of the launch scripts add **-ii** (which causes all required and optional flags to be prompted for) so adding -i to the launch scripts will result in the following error:

```
$ bash/probePod -v -i
Exception in thread "main" org.symphonyoss.symphony.tools.rest.util.command.CommandLineParserFault: 
  Switch "i" may be set at most 2 times
```
There are some [Common Switches](../CommonSwitches.md) which can be applied to most commands.

### Flags
A Flag is a named parameter. From the command line flags are entered as a parameter with double hyphen or a Unicode em-dash as an introducer and a variable length name, followed by a separate parameter containing the flag value. So a command which has a flag called storetype which also takes the switches above, could be entered as:

_example -qa --storetype pkcs12_

There are some [Common Flags](../CommonFlags.md) which can be applied to several commands.
