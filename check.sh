#!/usr/bin/env bash

set -e

# Environment
export JSR308=$(cd $(dirname "$0")/../ && pwd)
export CF=${JSR308}/checker-framework
export JAVAC=${CF}/checker/bin/javac
export CFCHECKER=$(cd $(dirname "$0")/ && pwd)

# Dependencies
export CLASSPATH=${CFCHECKER}/build/classes/java/main:${CFCHECKER}/build/resources/main:\
${CFCHECKER}/build/libs/configfile-checker.jar

# Command
DEBUG=""
CHECKER="org.checkerframework.checker.configfile.ConfigFileChecker"

declare -a ARGS
for i in "$@" ; do
    if [[ ${i} == "-d" ]] ; then
        echo "Typecheck using debug mode. Listening at port 5005. Waiting for connection...."
        DEBUG="-J-Xdebug -J-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
        continue
    fi
    ARGS[${#ARGS[@]}]="$i"
done

cmd=""

# Note:
# -Aignorejdkastub
# -Astubs=stubs/
# -AstubDebug

if [[ "$DEBUG" == "" ]]; then
    cmd="$JAVAC -cp "${CLASSPATH}" -processor "${CHECKER}" "${ARGS[@]}""
else
    cmd="$JAVAC "${DEBUG}" -cp "${CLASSPATH}" -processor "${CHECKER}" "${ARGS[@]}""
fi

eval "${cmd}"
