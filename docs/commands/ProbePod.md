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