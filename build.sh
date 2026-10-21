#!/bin/bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
chmod +x gradlew
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --stacktrace
