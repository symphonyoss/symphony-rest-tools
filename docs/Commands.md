---
nav-sort: 600
---
# Command Descriptions
Various commands are available either from the command line or the GUI. The following pages describe each command.
## Discovery Commands
### Probe Pod
The [Probe Pod](Commands/ProbePod.md) command attempts to discover the correct configuration parameters for a pod given a host name. It does this by probing various hostname and port number combinations
looking for well known API endpoints. If it is provided with a client certificate then it
will attempt to authenticate using that certificate, which if successful greatly increases
the chances of an accurate result.

Even without a certificate this command is able to create a correct configuration in
most cases.

## Diagnostic Commands
### Check Pod
The **Check Pod** command calls health check and other API endpoints to assess the
health or otherwise of a Pod. If an authentication certificate is provided then a 
more detailed assessment can be carried out but even without this a number of checks can be performed.

## Check Certs
The **Check Certs** command attempts to authenticate and performs a set of validity checks
on the provided client certificate and trust store and the server certificates presented
by the various server endpoints. This command is intended to provide a clear and reliable
assessment of the validity of the client certificate and trust store and to distinguish
the various failure modes more easily and definitively than can be conveniently done by
examination of log files.
