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

## Download and install the Appengine SDK files
https://cloud.google.com/appengine/downloads

Download and extract the AppEngine SDK. Current Initium-ODP and Cached-Datastore repos use version 1.9.36, although you can likely use a newer version (though untested).

Once you have the appengine SDK on your system, you may need to tell the GWT Appengine plugin in eclipse where your SDK is. To do the following: 

Go to: Window | Preferences | Google | App Engine
Click on: Add...
Choose the installation directory of your freshly installed Appengine SDK
That should do it.

## Get this GIT repo onto your system
I personally use TortoiseGIT. You can download that here: 
https://tortoisegit.org/

Before you clone the repo, create a fork of this project in GitHub, then create an upstream repo to link back to the root.
From your own fork, click the "Clone or download" button, and copy the path (typically https://github.com/*username*/initium-odp.git). Right-click a folder in Explorer, and select `Git Clone...`. Paste the link you copied in URL, and specify the directory where you'll be working on your fork. 
After it's pulled from GitHub, we need to set the upstream repo, so right-click the directory of your clone and go to `TortoiseGIT`->`Settings`. Select `Git`->`Remote`. Type in `upstream` in the Remote textbox, and `https://github.com/Emperorlou/initium-odp.git` in the URL textbox, then click the `Add New/Save` button.

Command line setup is a lot easier, but requires a Git installation (ie: git-for-windows.github.io).
In command line, navigate to the directory you want to download the clone to, and type the following commands:
```git clone https://github.com/*username*/initium-odp.git
git remote add upstream https://github.com/Emperorlou/initium-odp```

That's it.

## Get the CachedDatastore repo as well
https://github.com/Emperorlou/Cached-Datastore

You only need a clone of this repo, don't create a fork. No changes should be made to this repo unless instructed by another dev (specifically the creator).

## Import CachedDatastore AND Initium-ODP as a project into your eclipse
These directions assume a fresh install. 

###Import the Cached-Datastore repo first.###
- Within Eclipse, go to `File`->`Import...`
- Select `Projects from Git` (just type in `Git` in the filter if you can't find it), click Next. 
- `Select Repository Source`: Select `Local` and click Next. 
- `Select a Git Repository`: Select a repo and click Next. 
  - There probably won't be any listed here, so click on Add, browse to the root of the cloned repo, click Finish.
- `Select a wizard to use for importing projects`: Select `Import existing projects` in the wizard, and click Next.
- `Import Projects`: Make sure the repo is checked, optionally add the project to a working set (if you created one), and click Finish.

###Repeat the steps for the Initium-ODP repo###

It will likely complain that it can't find the references to the AppEngine SDK, so you will need to fix both projects. To do this do the following:
 - Right click on the project in your Project Explorer in Eclipse
 - Go to Google
 - Go to App Engine Settings..
 - Uncheck **Use Google App Engine**
 - Press OK (you should get an error message)
 - Re-check **Use Google App Engine**
 - Press OK
 
If there are any errors regarding missing AppEngine libraries (missing from specified path), then you will likely have to update the build paths for each of the reference AppEngine libraries. Manually update the paths in `.classpath` file of both projects, as doing it through the Eclipse IDE is not pleasant.

## Improve this documentation
If there was ANYTHING that didn't work as expected, please update this documentation to reflect that and how to solve the issue. THANKS!


My appologies in advance for the lack of a proper build process and manager!


