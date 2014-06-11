#!/bin/bash

destdir=~/Documents/git/sesame/core/http/workbench/src/main/webapp/scripts
srcdir=$destdir/ts
cd $srcdir
tsc --noImplicitAny --sourcemap --sourceRoot "/openrdf-workbench/scripts/ts" --outDir $destdir *.ts
echo 'Replaced repository JavaScript files with compiled TypeScript versions.'
