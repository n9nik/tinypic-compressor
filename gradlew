#!/bin/sh
# Gradle wrapper script
GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$HOME/.gradle"}
exec java -jar "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"
