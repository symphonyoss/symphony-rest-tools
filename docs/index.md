# symphony-rest-tools
This project provides a set of low level tools and diagnostics for use with the Symphony public REST API.  Although the symphony-rest-tools projects are implemented primarily in Java, they are intended to support development of API clients in any language, and to be a useful tool in understanding the interaction of higher level abstractions and language specific bindings with the underlying REST endpoints. 

This project therefore attempts to expose all of the details and features of the REST API endpoints, some of which might be purposefully hidden or abstracted away by other language specific API bindings. It is possible to call any API endpoint using any HTTP client, and this project attempts to provide easy ways to make ad hoc calls to ny API endpoint. This means that it is possible to implement client applications directly using the tools provided by this project, and for some small use cases this may be convenient and appropriate.

Notwithstanding the above, this project is intended to be a development and diagnostic kit bag rather than an application development framework, and it is specifically not intended to supersede or replace the Symphony Java Client or any other language bindings for application development against the public API.

This project uses JCurl as the HTTP client and leverages the JSON parsing capabilities
of that project. This project provides a binary distribution with convenience scripts to 
allow of the ad hoc exercising of various API endpoints from the command line.

The **symphony-rest-tools-ui** sister project provides an Eclipse based UI for this
project which allows capabilities of this project to be exercised in a UI context.

## Architecture
In order to facilitate the implementation of REST Tools capabilities across the command
line and UI contexts, a common architecture is used to represent the various capabilities
provided.

### Commands
A **Command** is a distinct piece of functionality which can be executed by itself. This project provides convenience scripts to allow each command to be executed in an ad hoc way from the command line. The **symphony-rest-tools-ui** project exposes commands the menus and toolbars.

In order to facilitate the execution of commands from various contexts, a command follows a set of pre-defined phases:

- Gathering of input parameters (Switches and Flags)
- Execution with progress reporting
- Reporting of results (Objectives)

### Switches
A switch is an input which has a boolean state. From the command line switches are set with a parameter with a single hyphen introducer and a single letter name. Switches can be concatenated so a command which takes the switch **q** to enable quiet mode and **a** to report all values could be executed from the command line as

_example -q -a_

or equally as

_example -qa_

Some flags may be set more than once to increase their effect, for example the standard flag **v** can be used to request that a command produces verbose output and this can be repeated 2 or 3 times for even greater levels of verboseness, e.g:

_example -vvv_

The following common switches can be applied to most commands:

| Flag | Multiple Allowed | Meaning																																 |
|------|------------------|--------------------------------------------------------------------------|
| v    | Yes							 | 	Verbose mode |
| i    | Yes							 | 	Interactive mode, once causes all required parameters to be prompted for twice causes optional parameters to be prompted as well. | 

### Flags
A Flag is a named parameter. From the command line flags are entered as a parameter with double hyphen or a Unicode em-dash as an introducer and a variable length name, followed by a separate parameter containing the flag value. So a command which has a flag called storetype which also takes the switches above, could be entered as:

_example -qa --storetype pkcs12_

### Objectives
An objective is a named item which describes one of the purposes of a command. Each Objective has a state and one or more messages associated with it. At the completion of a command all of the objectives are reported along with the overall status of that objective.

The **Component Status** page describes the varios status values and their meaning.

## Configuration
A configuration directory, referred to as **SRT_HOME** is used to store configuration
details for these tools. By default this directory is a folder named **.srt** in the
user's home directory.

This configuration includes dynamic session credentials as well as the static configuration
to identify the appropriate server endpoints needed to access the APIs. For this
reason the configuration directory is created read only to the owner and should
be treated as sensitive.

This is intended to be useful as a development tool so that session credentials
can be shared among heterogeneous implementations. 

See the **Configuration** Page for more details.

## Discovery Commands
### Probe Pod
The **Probe Pod** command attempts to discover the correct configuration parameters for a pod given a host name. It does this by probing various hostname and port number combinations
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
