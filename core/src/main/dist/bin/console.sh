#!/bin/sh

lib="$(dirname "${0}")/../lib"
java -cp "$lib/$(ls "$lib"|xargs |sed "s; ;:$lib/;g")" org.openrdf.console.Console
