#!/usr/bin/env bash
# distribution environment setup, replaces src/main/bash/environment.sh
# in distribution package

baseDir=`dirname $scriptDir`

java_classpath="${baseDir}/plugins/*:${baseDir}/lib/*"