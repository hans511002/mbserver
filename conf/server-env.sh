#!/usr/bin/env bash

# Set environment variables here.

# This script sets variables multiple times over the course of starting an hbase process,
# so try to keep things idempotent unless you want to take an even deeper look
# into the startup scripts (bin/hbase, etc.)

# The java implementation to use.  Java 1.6 required.
# export JAVA_HOME=/usr/java/jdk1.6.0/
. ~/.bash_profile

# Extra Java CLASSPATH elements.  Optional.
bin=`dirname $0`
bin=`cd "$bin"; pwd`

if [ "x$APP_HOME" == "x" ] ; then
   export APP_HOME=`cd "$bin/../"; pwd`
fi
. $APP_HOME/conf/appset

export COLLECT_CLASSPATH=$APP_HOME/conf

#for f in $APP_HOME/lib/*.jar ; do
#    COLLECT_CLASSPATH=${COLLECT_CLASSPATH}:$f;
#done
export COLLECT_CLASSPATH
if [ "$COLLECT_PID_DIR" == "" ] ; then
   export COLLECT_PID_DIR=$APP_HOME/logs
fi
if [ "$COLLECT_LOG_DIR" == "" ] ; then
   export COLLECT_PID_DIR=$APP_HOME/logs
fi

paramas="$@ "

openDebug=false
 
if [ "$paramas" != "${paramas/ -d /}" -o "$paramas" != "${paramas/ -debug /}" ] ; then
    openDebug=true
fi
debugPort=${debugPort:=8000}

if [ "$openDebug" = "true" ] ; then
    export DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$debugPort"
    if [ "$DEBUG" != "" ] ; then
        ports=`ps -ef|grep "Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="|grep -v grep|sed -e "s|.*suspend=n,address=||" -e "s|-XX:OnOutOfMemoryError.*||"`
        maxPort=$((--debugPort))
        for port in $ports ; do
            if [ "$port" -gt "$maxPort" ] ; then
               maxPort=$port
           fi
        done
        ((maxPort++))
        DEBUG="${DEBUG/$debugPort/$maxPort}"
    fi
   # echo "DEBUG=$DEBUG"
fi
