#!/bin/sh

export SBT_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"

sbt -jvm-debug 9999