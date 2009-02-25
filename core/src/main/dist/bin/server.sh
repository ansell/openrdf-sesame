#!/bin/sh

lib="$(dirname "${0}")/../lib"
java -mx512m -cp $lib/$(ls "$lib"|xargs |sed "s; ;:$lib/;g") org.openrdf.http.server.SesameServer $*
