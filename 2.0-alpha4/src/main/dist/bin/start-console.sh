#!/bin/sh

cd "`dirname "${0}"`/../lib"
java -jar openrdf-console-${version}.jar $1 $2 $3 $4 $5 $6 $7 $8 $9
cd ..
