---
# layout: tactile
layout: initializr
title: Quick Start Guide
---

# {{ page.title }}

Currently there are no binary releases of the TimeBench library.
Therefore, you need to download the [source code from GitHub](https://github.com/ieg-vienna/TimeBench) and compile it yourself.
We recommend that you clone the git repository, so that you can receive updates more easily.

To make your life easier, we provide a [guideline for Eclipse](#toc_2) below.

## Requirements

TimeBench is written in Java 1.6. To compile the TimeBench code, and to build
and run Visual Analytics prototypes, you'll need a copy of the Java Development
Kit (JDK) for version 1.6 or greater.

The library depends on the following packages:

- [prefuse](https://github.com/ieg-vienna/Prefuse), with some extensions
- [Apache Commons Lang 3 classes](http://commons.apache.org/proper/commons-lang/)
- [Apache log4j 1.2](http://logging.apache.org/log4j/1.2/)
- [iCal4j 1.0.4](http://sourceforge.net/projects/ical4j/)
- Java/R Interface (JRI), which is part of [rJava](http://www.rforge.net/rJava/)
- [ieg-util](https://github.com/ieg-vienna/ieg-util), general Java utilities
- [ieg-prefuse](https://github.com/ieg-vienna/ieg-prefuse), general prefuse extensions


## Guideline

We assume you use a current version of the [**Eclipse IDE**](http://www.eclipse.org/downloads/)
including at least the _Eclipse Java Development Tools_ and the _Eclipse Git Team Provider_.

