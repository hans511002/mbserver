#!/usr/bin/env bash
#

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin">/dev/null; pwd`

$bin/installer master start
