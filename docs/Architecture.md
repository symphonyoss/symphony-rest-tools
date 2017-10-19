---
nav-sort: 10
nav-level: 1
---
# Commands, Switches, Flags and Objectives
In order to facilitate the implementation of REST Tools capabilities across the command
line and UI contexts, a common architecture is used to represent the various capabilities
provided.

## Commands
A **Command** is a distinct piece of functionality which can be executed by itself. This project provides convenience scripts to allow each command to be executed in an ad hoc way from the command line. The **symphony-rest-tools-ui** project exposes commands the menus and toolbars.

In order to facilitate the execution of commands from various contexts, a command follows a set of pre-defined phases:

- Gathering of input parameters (Switches and Flags)
- Execution with progress reporting
- Reporting of results (Objectives)

## Switches
A switch is an input which has a boolean state. From the command line switches are set with a parameter with a single hyphen introducer and a single letter name. Switches can be concatenated so a command which takes the switch **q** to enable quiet mode and **a** to report all values could be executed from the command line as

_example -q -a_

or equally as

_example -qa_

Some flags may be set more than once to increase their effect, for example the standard flag **v** can be used to request that a command produces verbose output and this can be repeated 2 or 3 times for even greater levels of verboseness, e.g:

_example -vvv_

There are some [Common Switches](CommonSwitches.md) which can be applied to most commands.

## Flags
A Flag is a named parameter. From the command line flags are entered as a parameter with double hyphen or a Unicode em-dash as an introducer and a variable length name, followed by a separate parameter containing the flag value. So a command which has a flag called storetype which also takes the switches above, could be entered as:

_example -qa --storetype pkcs12_

There are some [Common Flags](CommonFlags.md) which can be applied to several commands.
