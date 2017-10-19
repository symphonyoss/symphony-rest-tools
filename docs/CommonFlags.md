---
nav-sort: 30
nav-level: 2
---
# Common Flags
The following common flags can be applied to several commands:

## --keystore
File name of a keystore file containing a client certificate for authentication.

## --storetype
The type of the keystore file (ignored if the keystore is not set), default pkcs12.

## --storepass
The password for the specified keystore (ignored if the keystore is not set), default changeit.

## --truststore
File name of a keystore file containing trusted signing and server certificates against which server certificates will be validated.

## --trusttype
The type of the truststore file (ignored if the truststore is not set), default jks.

## --trustpass
The password for the specified truststore (ignored if the truststore is not set), default changeit.
