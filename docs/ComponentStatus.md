# Component Status
A Component is part of an application or system which can report status. [ComponentStatus](../org.symphonyoss.symphony.tools.rest/src/main/java/org/symphonyoss/symphony/tools/rest/model/osmosis/ComponentStatus.java) is a Java Enum which is used to represent valid status values.

Component Status values are also used to report the outcome of Command Objectives.

## NotReady
This status indicates that the actual status of the component is not known or cannot be established. Monitoring systems will report this status for components which are unreachable over a network.

## Initializing
This is the default status for a component when it is created. Components should normally set their status to **Starting** as quickly as possible once they begin to execute.

## Starting
A state indicating that a process is running but has not yet completed all start up tasks.

## OK
This is the normal running status for a component or a successfully completed Objective. This status indicates that everything is fully normal.

## Warning
This status indicates that a process remains fully functional and is providing a complete service, but that some abnormal condition exists which puts the continued operation of the process or service at risk. For example, a server whose file system is almost full might report its status as **Warning**.

## Error
This status indicates that a process or service is not working fully, but is still able to provide a partial service. Unlike the **Warning** status, the **Error** status indicates that service is being impacted.

## Failed
This status indicates that the component has completely failed and is providing no significant service capability.

## Stopping
This status indicates that the component is in the process of an orderly shut down.

## Stopped
This status indicates that the component or service is stopped.
