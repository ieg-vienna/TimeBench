---
# layout: tactile
layout: initializr
title: Getting Started
short_title: Start
category: help
weight: 10
---

# {{ page.title }}

Currently there are no binary releases of the TimeBench library.
Therefore, you need to download the [source code from GitHub](https://github.com/ieg-vienna/TimeBench) and compile it yourself.
We recommend that you clone the git repository, so that you can receive updates more easily.

The TimeBench project on GitHub includes neither external depended libraries nor IDE-specific files,
so that _git_-based collaboration works more efficiently.

To make our and your life easier, we provide a [guideline for Eclipse](#toc_2) below.


## Requirements

TimeBench is written in Java 1.6. To compile the TimeBench code, and to build
and run Visual Analytics prototypes, you'll need a copy of the Java Development
Kit (JDK) for version 1.6 or greater.

The library depends on the following packages:

- [Apache Commons Lang 3 classes](http://commons.apache.org/proper/commons-lang/)
- [Apache log4j 1.2](http://logging.apache.org/log4j/1.2/)
- [iCal4j 1.0.4](http://sourceforge.net/projects/ical4j/)
- Java/R Interface (JRI), which is part of [rJava](http://www.rforge.net/rJava/)

Furthermore TimeBench depends on the following packages maintained by us:

- [prefuse](https://github.com/ieg-vienna/Prefuse), with some extensions
- [ieg-util](https://github.com/ieg-vienna/ieg-util), general Java utilities
- [ieg-prefuse](https://github.com/ieg-vienna/ieg-prefuse), general prefuse extensions


## Guideline

We assume you use a current version of the [**Eclipse IDE**](http://www.eclipse.org/downloads/)
including at least the _Eclipse Java Development Tools_ and the _Eclipse Git Team Provider_.

__Attention:__ Most steps are trivial or simply accepting defaults, but a few require special attention.
Better follow the guideline carefully.

1. Register at [GitHub](https://github.com/)
    and _(optional)_ [setup SSH keys](https://help.github.com/articles/generating-ssh-keys).
    Otherwise you can use the `HTTPS clone URL` as URI.

1. TimeBench

    2. Clone TimeBench to your computer and add it to Eclipse
        1. in Eclipse select `File` -> `Import`
        2. then `Git` -> `Projects from Git`
        3. Select Repository Source: `Clone URI`
        4. Source Git Repository: set `URI` to `git@github.com:ieg-vienna/TimeBench.git`<br>
            Do not change anything else! Leave user `git` and password ` `!
        5. Branch Selection: you only need `master` and can add others later
        6. (only at first use of Eclipse Git Team Provider) Select a directory where git should store local repositories.
            This should be outside your Eclipse workspace directory!<br>
            For example, I use `/home/rind/scm`.
        7. Local Destination: accept default values
        8. __Attention!__ Select a wizard to use for importing projects: `Import as general project`
        9. Import projects: accept default -- we will overwrite that in a minute

        Alternatively, you can use your favorite git client and then import as `Existing Projects into Workspace`.

    3. Convert TimeBench to a Java project

        The repository contains templates of Eclipse project files `.project` and `.classpath`
        but the actual project files should not be added to git.

        1. __Attention!__ Outside of Eclipse, copy `eclipse.project` to `.project` replacing the existing file
            and copy `eclipse.classpath` to `.classpath`

            Windows users can copy this [BATCH file](downloads/make-eclipse-project.bat) into the project folder and run it there.

            Users of other operating systems can use their favorite file browser or shell.

            _Warning:_ Do not delete or rename the template files `eclipse.project` and `eclipse.classpath`.

        2. Afterwards select `Refresh` on the project in Eclipse.

2. Packages maintained by the TimeBench team _(optional)_

    These packages are maintained by the same people working on TimeBench
    and are used for general utilities and extensions to prefuse
    that can also be used without TimeBench.

    Since updates in TimeBench often require updates in these packages
    it makes sense to clone them as well as Eclipse projects.
    In that case, follow the same steps as above for:

    2. [ieg-util](https://github.com/ieg-vienna/ieg-util): `git@github.com:ieg-vienna/ieg-util.git`
    3. [ieg-prefuse](https://github.com/ieg-vienna/ieg-prefuse):`git@github.com:ieg-vienna/ieg-prefuse.git`
    1. [prefuse-vienna](https://github.com/ieg-vienna/Prefuse): `git@github.com:ieg-vienna/Prefuse.git`<br>
        __Attention!__ For *prefuse-vienna* use the `Import existing project` wizard and skip the step "Convert to a Java project".

    Otherwise, you can download them together with the external dependencies (see next step).

3. External dependencies

    We provide external dependencies as a ready to use Eclipse project.
    If you are using TimeBench with Eclipse you can follow these steps:

    1. Download the [ZIP archive](http://www.ifs.tuwien.ac.at/~rind/timebench/external_2013-07-27.zip).
    2. in Eclipse select `File` -> `Import`
    3. then `General` -> `Existing Projects into Workspace`
    4. Choose `Select archive file` and `Browse...` for the downloaded ZIP file.

    ___DISCLAIMER:___ We provide these packages solely as a convenience for developers interested in TimeBench.
        They is by no means guaranteed to be complete, up-to-date, or secure.


## Why Not Simply Pack It All Together?

Here are some of our motivations why we split it up that way:

- We often use the same external packages in multiple projects.
- Adding JAR files to a Git repository permanently increases the storage footprint
    of each clone -- even after the file is replaced with a newer version.
- While all of us work with Eclipse, there are many other great IDEs out there.
- Sometimes one has to adapt the project properties or the classpath in one's local workspace
    and, typically, these changes are not intended for anybody else.
    If the project files were under version control, such adaptions might be committed accidentally.
    Furthermore, some git operations are not possible with uncommitted changes in the working directory.
