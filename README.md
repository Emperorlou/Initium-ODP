# Initium-ODP
This project allows people to contribute to the parts of Initium that are open source.                

# How to get your development environment setup
It's possible to get things working using a different IDE and other different variations (like JDK version), but I'll try to give you precise instructions for getting things working the way I know how.  

**Please let me know if anything doesn't work for you so I can update the documentation!**

## Download and install JDK 1.7
(Only if you don't already have it)
You should be able to find what you need here:
http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html
You’ll want to download the “Java SE Development Kit 7u80” (It’s possible that you’ll be prompted to create an Oracle account) 

## Get this GIT repo onto your system
I personally use TortoiseGIT. You can download that here: 
https://tortoisegit.org/

(If you’re using Mac OSX, you’ll need an alternative to TortoiseGIT. SourceTree is a great option, found at https://www.sourcetreeapp.com/)

Before you clone the repo, create a fork of this project in GitHub (you will then create an upstream repo to link back to the root).

From your own fork, click the "Clone or download" button, and copy the path (typically https://github.com/*username*/initium-odp.git). Right-click a folder in Explorer, and select `Git Clone...`. Paste the link you copied in URL, and specify the directory where you'll be working on your fork. 

After it's pulled from GitHub, we need to set the upstream repo, so right-click the directory of your clone and go to `TortoiseGIT`->`Settings`. Select `Git`->`Remote`. Type in `upstream` in the Remote textbox, and `https://github.com/Emperorlou/initium-odp.git` in the URL textbox, then click the `Add New/Save` button.

Command line setup is a lot easier, but requires a Git installation (ie: git-for-windows.github.io).
In command line, navigate to the directory you want to download the clone to, and type the following commands:

```
git clone https://github.com/*username*/initium-odp.git
git remote add upstream https://github.com/Emperorlou/initium-odp.git
```

That's it.

## Get the rest of the code you'll need and get gradle 
To do this simply run the odp-clone.bat file in the root of this repo that should now be on your system.

## Import Initium-ODP as a gradle project into your IDE of choice
Exact instructions for this depend on the IDE you wish to use. The IDE will need Gradle support to work so if you're using an older version of eclipse you may want to upgrade (better gradle support) or install a plugin.

## Improve this documentation
If there was ANYTHING that didn't work as expected, please update this documentation to reflect that and how to solve the issue. THANKS!


My appologies in advance for the lack of a proper build process and manager!


