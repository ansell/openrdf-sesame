#!/bin/sh

cd "`dirname "${0}"`/../lib"
java -jar openrdf-console-2.0-beta2.jar $1 $2 $3 $4 $5 $6 $7 $8 $9
cd ..
