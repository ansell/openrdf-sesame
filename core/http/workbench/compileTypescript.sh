#!/bin/bash
scriptdir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
destdir="$scriptdir/src/main/webapp/scripts"
srcdir=$destdir/ts
cd $srcdir
tsc --noImplicitAny --sourcemap --sourceRoot "/openrdf-workbench/scripts/ts" --outDir $destdir *.ts
echo 'Replaced repository JavaScript files with compiled TypeScript versions.'
