#Welcome to the Sesame code repository#

Sesame is an extensible Java framework for storing, querying and inferencing for RDF. It can be deployed as a web server or used as a Java library. Features include several query languages (SeRQL and SPARQL), inferencing support, and RAM or disk storage. Additionally, the central APIs of Sesame are storage-independent and are supported by many third-party RDF database vendors.

Report any issues in the [OpenRDF JIRA issue tracker](https://openrdf.atlassian.net/). 

More information about Sesame can be found at [openRDF.org](http://www.openrdf.org/).

##How to contribute##

Sesame development typically takes place on one of the release branches (for example, branch 2.7.x or 2.8.x). The core development team fixes issues by creating *feature branches* (typically with the number of corresponding JIRA issue in the branch name) off of the appropriate release branch. Once a fix is tested it is merged back into the release branch.

If you have a minor patch to contribute, you can do so by forking the repository, fixing the issue in your fork, and then submitting a pull request for us to merge your fixes into the core repository. If you do this, please make sure you submit the
pull request against the correct branch (99 out of a 100 times it should be the latest release branch, _not_ the master branch). If unsure, get in touch with the development team. 

For larger contributions, please get in touch with the development team first. Explain your fix or improvement and we can look at the best way to incorporate it together.

##Acknowledgements##

No Open Source project can exist without a community to support it. We
therefore gratefully list the people and organizations at the heart of the
OpenRDF community.

###Atlassian Support###

[Atlassian](http://www.atlassian.com/) have generously contributed an Open Source license for us to use their awesome JIRA platform as our [issue tracker](https://openrdf.atlassian.net/). 
 
###Organizations contributing to Sesame###

In no particular order, these companies and organizations have all contributed to Sesame development:

* [Aduna](http://www.aduna-software.com/)
* [Stichting NLnet](http://www.nlnet.nl/)
* [Ontotext](http://www.ontotext.com/)
* [Fluid Operations](http://www.fluidops.com/)
* [3 Round Stones](http://www.3roundstones.com/)
* [Clark & Parsia](http://www.clarkparsia.com/)
* [Institute for Defense Analyses (IDA)](https://www.ida.org/)

###Individual Developers###

The following people are currently involved as developers of Sesame, or have been in the past (listed in no particular order): Arjohn Kampman, Jeen Broekstra, James Leigh, Herko ter Horst, David Huynh, Peter Mika, Peter Ansell, Gunnar Aastrand Grimnes, Leo Saurmann, Sebastian Weber, Michael Grove, Dale Visser, Andreas Schwarte, Jerven Bolleman, Philip Coates, Joseph Walton, Jakob Frank, Fernando Hernandez, Nikola Petrov, Sebastian Schaffert, Roland Stühmer, and Andrew MacKinlay.

In addition, we want to gratefully acknowledge all members of the Sesame community who have come up with new ideas, bug reports, and patches, and so have helped carry this project forward. Cheers!

##Copyright and License##

This work is licensed to Aduna under one or more contributor license
agreements. See the [NOTICE.txt](/openrdf/sesame/src/master/core/NOTICE.txt)
file distributed with this work for additional information regarding copyright
ownership. 

Aduna licenses this work to you under the terms of the Aduna BSD License (the
"License"); you may not use this file except in compliance with the License.
See the [LICENSE.txt](/openrdf/sesame/src/master/core/LICENSE.txt) file
distributed with this work for the full License.
