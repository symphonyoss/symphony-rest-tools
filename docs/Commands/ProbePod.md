---
nav-sort: 410
nav-level: 1
---
# Probe Pod

The Probe Pod command attempts to discover the correct configuration parameters for a pod given a host name. It does this by probing various hostname and port number combinations looking for well known API endpoints. If it is provided with a client certificate then it will attempt to authenticate using that certificate, which if successful greatly increases the chances of an accurate result.

Even without a certificate this command is able to create a correct configuration in most cases.
