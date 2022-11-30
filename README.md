# DEXSLib
![version](https://img.shields.io/badge/version-0.7.1-blue) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.fkie-cad/DEXSlib/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.fkie-cad/DEXSlib)

This project aims as an open source library for static backward and forward slicing of Smali code when reversing Android applications. 

It is build upon [dexlib2](https://github.com/JesusFreke/smali/tree/master/dexlib2).

**Currently, the project is being heavily refactored and is therefore in an unusable state**

## Why Slicing?

When it comes to reverse engineering 3rd party, closed, binary Android apps - espcially Android malware - we often have the need to analyse the code based on certain methods or instructions. The direction might be either forward or backwards. 

## Using the library

DEXSLib can now be found on Maven Central. In order to use DEXSLib in your gradle build, include the following line into your gradle.build:
```
dependencies {
...
    implementation 'io.github.fkie-cad:DEXSlib:0.7.1'
...
}
```
We recommend using the latest and greatest version unless you have a specific issue that prevents you from doing so.


## Contribute

Contributions are always welcome. Just fork it and open a pull request!.
___

## Support

If you have any suggestions, or bug reports, please create an issue in the Issue Tracker.

In case you have any questions or other problems, feel free to send an email to:

[daniel.baier@fkie.fraunhofer.de](mailto:daniel.baier@fkie.fraunhofer.de).
