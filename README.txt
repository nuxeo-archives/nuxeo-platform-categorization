Nuxeo document categorization
=============================

Automated document categorization addon for the Nuxeo platform and derived
applications. Categorization suggestions are based upon the textual content

:author: Olivier Grisel <ogrisel@nuxeo.com>


Project overview
----------------

The sub-project ``nuxeo-platform-categorization-service`` provides a
service to register pluggable document categorizers for various properties
along with interface definitions and base implementation classes.

The ``nuxeo-platform-categorization-{language,subjects,coverage}``
sub-projects provide pretrained models to provide suggestions for
respectively the dublincore language, subjects and coverage fields based
on the controlled list of possible values defined in the default Nuxeo
vocabularies. dc:subjects and dc:coverage models are trained on a english
wikipedia based corpus and hence are not expected to perform well with
other languages.


Building from source and deployement
------------------------------------

Download the source::

  $ hg clone https://hg.nuxeo.org/addons/nuxeo-platform-categorization
  $ cd nuxeo-platform-categorization
  $ hg clone https://hg.nuxeo.org/addons/nuxeo-platform-categorization/nuxeo-platform-categorization-subjects
  $ hg clone https://hg.nuxeo.org/addons/nuxeo-platform-categorization/nuxeo-platform-categorization-coverage
  $ hg clone https://hg.nuxeo.org/addons/nuxeo-platform-categorization/nuxeo-platform-categorization-language

The models are hold in their own mercural repositories in subfolders so as to
avoid polluting the history of the service package with large blobs that can
take a long time to download.

Download dependencies and build the jars (in each repo)::

  $ mvn install -Dmaven.test.skip=true

Copy the resulting jars from the "target" sub-folders into the ``bundles`` or
``plugins`` folder of your tomcat-based or jboss-based Nuxeo application and
restart::

  $ cp nuxeo-platform-categorization-{service,coverage,subjects,language}/target/*-SNAPSHOT.jar \
  /opt/nuxeo-dam-tomcat/nxserver/bundles/

The default listener configurations trigger synchronous categorizations
of all newly created or modified non-folderish documents if they have
empty ``dc:language``, ``dc:subjects`` and ``dc:coverage`` properties.


Categorizer implementations
---------------------------

The language categorizer is using the TextCat_ Java library with pre-trained
models for most european languages based on character n-grams fingerprints.

.. _TextCat:: http://textcat.sourceforge.net/

The other models are based on the ``TfIdfCategorizer`` implementation
found in the ``nuxeo-platform-categorization-service`` sub-project. This
class compute token level TF-IDF cosine similarities between text
documents. Read the class javadoc for more implementation details and
online references.


Building new models
-------------------


Building new ``TfIdfCategorizer``-based models
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

TODO: write a complete step by step procedure here

Step 1: extract raw text document with category names as
filenames using the mahout project: e.g. steps 1 to 5 of
http://cwiki.apache.org/MAHOUT/wikipediabayesexample.html

Step 2: use the ``main`` method of the ``TfIdfCategorizer`` class to analyze
raw text extractions from step 1.

Step 3: package the models ase a contribution to the categorization service by
taking ``nuxeo-platform-categorization-{subjects,coverage}`` as examples.


Building new language models
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Step 1: Follow the TextCat_ documentation to train the models

Step 2: Package the resulting model as a contribution to the categorizer service
by taking ``nuxeo-platform-categorization-language`` as an example.

