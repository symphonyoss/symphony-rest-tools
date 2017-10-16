# symphony-rest-tools
Low level tools for use with the Symphony public REST API

## ProbePod
This command line utility probes a host for the presence of 
a Symphony pod. If a client certificate is provided then it
attempts to authenticate and makes a call to the pod and agent
API endpoints. Without a certificate it will attempt to find
the configuration but has to assume that an endpoint configured
for client certificate authentication is a valid endpoint.

As the probe progresses detailed information about the results
is printed to stdout. If the probe is successful then a
summary is printed and the following files are created:

* A certificate truststore file containing the root certificates
provided by the various servers to which connections are made
* A Java properties file containing the various URLs and path
to the client certificate (if any). This file is compatible
with the [Symphony Java Client](https://github.com/symphonyoss/symphony-java-client)

### Usage:

```
probePod [--keystore certFile] [--storepass password] [--storetype keystoreType] [hostname]

```
--keystore certFile

The path to a file containing a client certificate, usually in
PKCS#12 format with a .p12 extension, if absent then no authentication
is attempted.

 --storepass password

The password for the provided keystore, default "changeit"

 --storetype keystoreType
 
 The format of the provided certificate file, default "pkcs12".
 
 hostname
 
 The hostname of the server to probe. If a simple name is provided then
 the domain .symphony.com is assumed.
 
 If no value is provided the the user is prompted to enter a value
 on the standard input.
 

### Example:

```
$ probePod.sh --keystore bot.user5.p12 nexus2
name=nexus2
domain=.symphony.com

Probing for Pod
===============
Probing https://nexus2.symphony.com/...
Root server cert CN=Go Daddy Secure Certificate Authority - G2,OU=http://certs.godaddy.com/repository/,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
End server cert CN=*.symphony.com,OU=Domain Control Validated
Probing https://nexus2.symphony.com//client/index.html...
Root server cert CN=Go Daddy Secure Certificate Authority - G2,OU=http://certs.godaddy.com/repository/,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
End server cert CN=*.symphony.com,OU=Domain Control Validated
Probing https://nexus2.symphony.com//webcontroller/HealthCheck...
Root server cert CN=Go Daddy Secure Certificate Authority - G2,OU=http://certs.godaddy.com/repository/,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
End server cert CN=*.symphony.com,OU=Domain Control Validated
We found a Symphony Pod!


Probe Successful
================
Pod URL             =https://nexus2.symphony.com
Pod ID              =130
Key Manager URL     =https://nexus2.symphony.com/relay
Session Auth URL    =https://nexus2.symphony.com:8444/sessionauth
Key Auth URL        =https://nexus2.symphony.com:8444/keyauth
Pod API URL         =https://nexus2.symphony.com/pod
Agent API URL       =https://nexus2.symphony.com/agent

Client cert         =/atlas/symphony/global/certs/bot.user1.p12
We authenticated as
userInfo.displayName=Bot User 1
userInfo.id         =8933531975687
userInfo.company    =Symphony Nexus Team Dev 2

Root server certs:
CN=Go Daddy Secure Certificate Authority - G2,OU=http://certs.godaddy.com/repository/,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
CN=Go Daddy Root Certificate Authority - G2,O=GoDaddy.com\, Inc.,L=Scottsdale,ST=Arizona,C=US
Truststore saved as /var/folders/1d/8g7d6xdx7_z4xj45j97kgk080000gn/T/server4920420444920238160.truststore
Config file saved as /var/folders/1d/8g7d6xdx7_z4xj45j97kgk080000gn/T/symphony2279410722209853728.properties

End server certs:
CN=*.symphony.com,OU=Domain Control Validated

```
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
## Contribute
This project was initiated at [Symphony Communication Services, LLC.](https://www.symphony.com) and has been developed as open-source from the very beginning.

Contributions are accepted via GitHub pull requests. All contributors must be covered by contributor license agreements to comply with the [Code Contribution Process](https://symphonyoss.atlassian.net/wiki/display/FM/Code+Contribution+Process).
