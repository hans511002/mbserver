#!/usr/bin/env bash
# Modelled after $APP_HOME/sbin/installer-env.sh.
. /etc/bashrc
bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin">/dev/null; pwd`

# the root of the installer installation
export APP_HOME=`cd "$bin/../"; pwd`
  
# override default settings for this command, if applicable
if [ -f "$APP_HOME/conf/server-env.sh" ]; then
  . "$APP_HOME/conf/server-env.sh"
  ECHO_TIPS load "$APP_HOME/conf/server-env.sh"
elif [ -f "$APP_HOME/sbin/server-env.sh" ]; then
  . "$APP_HOME/sbin/server-env.sh"
fi


if [ -z "$JAVA_HOME" -o ! -d "$JAVA_HOME" ]; then
  for candidate in \
    $APP_HOME/bin/jdk/linux/jdk1.7* \
    $APP_HOME/bin/jdk/linux/jdk1.8* ; do
    if [ -e $candidate/bin/java ]; then
      export JAVA_HOME=$candidate
      break
    fi
   done
fi
if [ -z "$JAVA_HOME" -o ! -d "$JAVA_HOME" ] ; then
    jdkFiles=`ls $APP_HOME/bin/jdk/linux/ | tail -n 1`
    cd $APP_HOME/bin/jdk/linux;
    tar xf $jdkFiles
fi

if [ -z "$JAVA_HOME" -o ! -d "$JAVA_HOME" ]; then
  for candidate in \
    $APP_HOME/bin/jdk/linux/jdk1* \
    /usr/lib/jvm/java-6-sun \
    /usr/lib/jvm/java-1.6.0-sun-1.6.0.*/jre \
    /usr/lib/jvm/java-1.6.0-sun-1.6.0.* \
    /usr/lib/j2sdk1.6-sun \
    /usr/java/jdk1.6* \
    /usr/java/jre1.6* \
    /usr/java/jdk1.7* \
    /usr/java/jre1.7* \
    /usr/java/jdk1.8* \
    /usr/java/jre1.8* \
    /Library/Java/Home ; do
    if [ -e $candidate/bin/java ]; then
      export JAVA_HOME=$candidate
      break
    fi
  done
  # if we didn't set it
  if [ -z "$JAVA_HOME" -o ! -d "$JAVA_HOME" ]; then
    cat 1>&2 <<EOF
+======================================================================+
|      Error: JAVA_HOME is not set and Java could not be found         |
+----------------------------------------------------------------------+
| Please download the latest Sun JDK from the Sun Java web site        |
|       > http://java.sun.com/javase/downloads/ <                      |
|                                                                      |
| installer requires Java 1.7 or later.                                    |
| NOTE: This script will find Sun Java whether you install using the   |
|       binary or the RPM based installer.                             |
+======================================================================+
EOF
    exit 1
  fi
fi
