# symphony-rest-tools
Low level tools for use with the Symphony public REST API

Full documentation is available at https://symphonyoss.github.io/symphony-rest-tools/

## Quick Start
Go to the releases page at https://github.com/symphonyoss/symphony-rest-tools/releases and download the latest version of symphony-rest-tools-cmdline-X.Y.Z-bin.tar.gz

This is the pure Java, platform neutral, command line implementation.

Create a directory in a convenient location and extract the tarball:

```
$ mkdir /tmp/srt
$ cd /tmp/srt
$ tar zxf ~/Downloads/symphony-rest-tools-cmdline-0.1.6-bin.tar.gz 
$ ls
bash	bat	certs	lib
```

The **bash** directory contains shell scripts for Unix based systems, the **bat** directory contains Windows batch file scripts which do the same thing for that platform.

The **probePod** command requires a host name and will attempt to discover the configuration of a pod on that host. Launch this script, enter a host name (.symphony.com is applied as a suffix if the entered value has no dots) and hit RETURN to accept the defaults for all other parameters:

```
$ bash/probePod 
Default home area "/Users/bruce/.srt" created.
SRT_HOME set by Default
Press RETURN to accept default values
Enter a space to clear the default value
Leading and trailing whitespace are deleted
Location of SRT home[]: 
Host Name[]: foundation-dev
Keystore File Name[]: 
Keystore Type[pkcs12]: 
Keystore Password[changeit]: 
Truststore File Name[]: 
Truststore Type[jks]: 
Truststore Type[changeit]: 
name=foundation-dev
domain=.symphony.com


Probing foundation-dev.symphony.com for a Pod
=============================================
Probing for Pod
===============
```

The script then attempts to connect to a variety of host name and port number combinations to locate a Symphony Pod:

```
Probing foundation-dev.symphony.com for a Pod: Probing Port 443
===============================================================
Probing https://foundation-dev.symphony.com/...
Root server cert CN=Go Daddy Root Certificate Authority - G2,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
End server cert CN=*.symphony.com,OU=Domain Control Validated
Probing https://foundation-dev.symphony.com/client/index.html...
Root server cert CN=Go Daddy Root Certificate Authority - G2,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
End server cert CN=*.symphony.com,OU=Domain Control Validated
Probing https://foundation-dev.symphony.com/webcontroller/HealthCheck...
Root server cert CN=Go Daddy Root Certificate Authority - G2,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
End server cert CN=*.symphony.com,OU=Domain Control Validated
We found a Symphony Pod!

Probing for API Sessionauth
===========================
Probing https://foundation-dev-api.symphony.com:8444/sessionauth/v1/authenticate...
Cannot connect to foundation-dev-api.symphony.com:8444
Probing https://foundation-dev-api.symphony.com:8445/sessionauth/v1/authenticate...
Cannot connect to foundation-dev-api.symphony.com:8445
Probing https://foundation-dev-api.symphony.com:8446/sessionauth/v1/authenticate...
Cannot connect to foundation-dev-api.symphony.com:8446
Probing https://foundation-dev.symphony.com:8444/sessionauth/v1/authenticate...
Cannot connect to foundation-dev.symphony.com:8444
Probing https://foundation-dev.symphony.com:8445/sessionauth/v1/authenticate...
Cannot connect to foundation-dev.symphony.com:8445
Probing https://foundation-dev.symphony.com:8446/sessionauth/v1/authenticate...
Cannot connect to foundation-dev.symphony.com:8446
Failed to find any Session Auth endpoint
Probing https://foundation-dev.symphony.com/pod/v2/sessioninfo...
Failed with HTTP status 401
JSON=null
Failed to connect to POD API
Probing https://foundation-dev.symphony.com/login/checkauth?type=user...
Root server cert CN=Go Daddy Root Certificate Authority - G2,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
End server cert CN=*.symphony.com,OU=Domain Control Validated
keyManagerUrl is https://foundation-dev.symphony.com/relay
Probing https://foundation-dev.symphony.com/webcontroller/public/podInfo...
Failed with HTTP status 401
Can't get podInfo from this Pod.
keyManagerName=foundation-dev
keyManagerDomain=.symphony.com
Found key manager at https://foundation-dev.symphony.com/relay

Probing for API Keyauth
=======================

Probing foundation-dev.symphony.com for a Pod: Probing for API Keyauth
======================================================================
Probing https://foundation-dev-api.symphony.com:8444/keyauth/v1/authenticate...
Cannot connect to foundation-dev-api.symphony.com:8444
Probing https://foundation-dev-api.symphony.com:8445/keyauth/v1/authenticate...
Cannot connect to foundation-dev-api.symphony.com:8445
Probing https://foundation-dev-api.symphony.com:8446/keyauth/v1/authenticate...
Cannot connect to foundation-dev-api.symphony.com:8446
Probing https://foundation-dev.symphony.com:8444/keyauth/v1/authenticate...
Cannot connect to foundation-dev.symphony.com:8444
Probing https://foundation-dev.symphony.com:8445/keyauth/v1/authenticate...
Cannot connect to foundation-dev.symphony.com:8445
Probing https://foundation-dev.symphony.com:8446/keyauth/v1/authenticate...
Cannot connect to foundation-dev.symphony.com:8446
Failed to find any Key Auth endpoint

Probing foundation-dev.symphony.com for a Pod: Probing for API Agent
====================================================================

Probing for API Agent
=====================
Probing https://foundation-dev-api.symphony.com/agent/v1/util/echo...
Certificate auth required for foundation-dev-api.symphony.com:443
Probing https://foundation-dev-api.symphony.com:8444/agent/v1/util/echo...
Cannot connect to foundation-dev-api.symphony.com:8444
Probing https://foundation-dev-api.symphony.com:8445/agent/v1/util/echo...
Cannot connect to foundation-dev-api.symphony.com:8445
Probing https://foundation-dev-api.symphony.com:8446/agent/v1/util/echo...
Cannot connect to foundation-dev-api.symphony.com:8446
Probing https://foundation-dev.symphony.com/agent/v1/util/echo...
Failed with HTTP status 400
Probing https://foundation-dev.symphony.com:8444/agent/v1/util/echo...
Cannot connect to foundation-dev.symphony.com:8444
Probing https://foundation-dev.symphony.com:8445/agent/v1/util/echo...
Cannot connect to foundation-dev.symphony.com:8445
Probing https://foundation-dev.symphony.com:8446/agent/v1/util/echo...
Cannot connect to foundation-dev.symphony.com:8446
OK
Found probable Agent API endpoint at https://foundation-dev-api.symphony.com/agent

Probe Successful
================
Web URL             =https://foundation-dev.symphony.com/
Pod URL             =https://foundation-dev.symphony.com
Pod ID              =0
Key Manager URL     =https://foundation-dev.symphony.com/relay
Session Auth URL    =null
Key Auth URL        =null
Pod API URL         =https://foundation-dev.symphony.com/pod
Agent API URL       =https://foundation-dev-api.symphony.com/agent

Client cert         =
This cert was not accepted for authentication

Root server certs:
CN=Go Daddy Root Certificate Authority - G2,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US

End server certs:
CN=*.symphony.com,OU=Domain Control Validated

Probing foundation-dev.symphony.com for a Pod: Saving Configuration
===================================================================
Finished.

```

Finally the results are displayed.

```
Objectives
==========
Locate Pod           OK         
Locate Session Auth Endpoint Failed     Unable to locate URL
Locate Pod API Endpoint OK         
Locate Key Manager   OK         
Locate Key Manager Auth Endpoint Failed     Unable to locate URL
Locate Agent         OK         


$ 

```
# Next Steps

Instructions for installing the various releases is available at

https://symphonyoss.github.io/symphony-rest-tools/GettingStarted



## Contribute
This project was initiated at [Symphony Communication Services, LLC.](https://www.symphony.com) and has been developed as open-source from the very beginning.

Contributions are accepted via GitHub pull requests. All contributors must be covered by contributor license agreements to comply with the [Code Contribution Process](https://symphonyoss.atlassian.net/wiki/display/FM/Code+Contribution+Process).
