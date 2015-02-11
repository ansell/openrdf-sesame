#Welcome to the Sesame code repository#

Sesame is an extensible Java framework for storing, querying and inferencing for RDF. It can be deployed as a web server or used as a Java library. Features include several query languages (SeRQL and SPARQL), inferencing support, and RAM or disk storage. Additionally, the central APIs of Sesame are storage-independent and are supported by many third-party RDF database vendors.

Report any issues in the [JIRA issue tracker](https://openrdf.atlassian.net/). 

More information about Sesame can be found at [the Sesame project website](http://rdf4j.org/).

##How to contribute##

Sesame development typically takes place on one of the release branches (for example, branch 2.7.x or 2.8.x). The core development team fixes issues by creating *feature branches* (typically with the number of corresponding JIRA issue in the branch name) off of the appropriate release branch. Once a fix is tested it is merged back into the release branch.

If you have a minor patch to contribute, you can do so by forking the repository, fixing the issue in your fork, and then submitting a pull request for us to merge your fixes into the core repository. If you do this, please make sure you submit the
pull request against the correct branch (99 out of a 100 times it should be the latest release branch, _not_ the master branch). If unsure, get in touch with the development team. 

For larger contributions, please get in touch with the development team first. Explain your fix or improvement and we can look at the best way to incorporate it together.

##Copyright and License##

This is a collective work, licensed under one or more contributor license
agreements. See the [NOTICE.txt](/openrdf/sesame/src/master/core/NOTICE.txt)
file distributed with this work for additional information regarding copyright
ownership. 

This work is licensed to you under the terms of a BSD-style License (the
"License"); you may not use this file except in compliance with the License.
See the [LICENSE.txt](/openrdf/sesame/src/master/core/LICENSE.txt) file
distributed with this work for the full License.