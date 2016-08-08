# Initium-ODP
This project allows people to contribute to the parts of Initium that are open source.  

# How to get your development environment setup
It's possible to get things working using a different IDE and other different variations (like JDK version), but I'll try to give you precise instructions for getting things working the way I know how. 

**Please let me know if anything doesn't work for you so I can update the documentation!**

## Download and install JDK 1.7
You should be able to find what you need here:
http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html

## Download Eclipse JUNO
Pick the first download labelled **Eclipse IDE for Java EE Developers**
https://eclipse.org/downloads/packages/release/juno/sr2

NOTE: It's possible that newer versions of eclipse will work without issue (or be even better), but I don't use them yet and my ability to provide support for issues you might encounter would be limited.

## Install the GWT Plugin for Eclipse
This plugin will give you the ability to develop appengine applications (it is not actually strictly for GWT). Appengine is what Initium uses for the backend.

This is the actual download link. Pick the **Eclipse 3.8/4.2 (Juno)**
https://developers.google.com/eclipse/docs/download_older

NOTE: Again, if you're using a newer version of eclipse, you'll want to pick the right version of the plugin for you.
Use this link to find the right version for you. 
This link also contains the install instuctions.
https://developers.google.com/eclipse/docs/getting_started

## Get this GIT repo onto your system
I personally use TortoiseGIT. You can download that here: 
https://tortoisegit.org/

## Get the CachedDatastore repo as well
https://github.com/Emperorlou/Cached-Datastore

## Import CachedDatastore AND Initium-ODP as a project into your eclipse
I've never actually done this step. Any ODP guys please update the documentation for how to do this precisely. Thanks!

NOTE: It may complain that there is no Appengine SDK defined. Usually that just means you have to remove the Appengine SDK and re-add it (silly eclipse). To do this do the following:
 - Right click on the project in your Project Explorer in eclipse
 - Go to Google
 - Go to App Engine Settings..
 - Uncheck **Use Google App Engine**
 - Press OK
 - Go back to that screen and re-check **Use Google App Engine**

## Improve this documentation
If there was ANYTHING that didn't work as expected, please update this documentation to reflect that and how to solve the issue. THANKS!


My appologies in advance for the lack of a proper build process and manager!


