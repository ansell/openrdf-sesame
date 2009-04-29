#!/bin/sh

JAVA_OPT=

lib="$(dirname "${0}")/../lib"
java $JAVA_OPT -cp "$lib/$(ls "$lib"|xargs |sed "s; ;:$lib/;g")" org.openrdf.http.server.Stop $*
