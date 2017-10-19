---
nav-sort: 300
nav-level: 0
---
# Configuration
A configuration directory, referred to as **SRT_HOME** is used to store configuration
details for these tools. By default this directory is a folder named **.srt** in the
user's home directory.

This configuration includes dynamic session credentials as well as the static configuration
to identify the appropriate server endpoints needed to access the APIs. For this
reason the configuration directory is created read only to the owner and should
be treated as sensitive.

This is intended to be useful as a development tool so that session credentials
can be shared among heterogeneous implementations. 
