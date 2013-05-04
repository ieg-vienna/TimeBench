TimeBench README
(snapshot for VAST 2013 submission; the source code will be moved to GitHub 
with the final publication)

THIS SOFTWARE DISTRIBUTION IS PROVIDED FOR SCIENTIFIC PEER-REVIEW ONLY AND IS 
SUBJECT TO VGTC ETHICS GUIDELINES FOR REVIEWERS, IN PARTICULAR CONFIDENTIALITY.
 
--INTRO--

TimeBench, a flexible, easy-to-use, and reusable software library written in 
Java that provides foundational data structures and algorithms for time-
oriented data in Visual Analytics. Thus, it eases the development and testing 
of new visualization, interaction, and analysis methods. It facilitates the 
reuse and combination of such methods and fosters their reproducibility and 
comparability.

TimeBench will be licensed under the terms of a BSD 2-clause license with the 
final publication.

--STRUCTURE--

The library distribution uses the following organization:

+ TimeBench
|-- data   Data files used by the demo applications
|-- demo   Demo applications showing the library in use
|-- lib    Third-party libraries used by TimeBench and their licenses
|-- src    The source code for the TimeBench library

--REQUIREMENTS--

TimeBench is written in Java 1.6. To compile the TimeBench code, and to build 
and run Visual Analytics prototypes, you'll need a copy of the Java Development 
Kit (JDK) for version 1.6 or greater. 

Besides the Java 1.6 core classes and prefuse, TimeBench depends on the 
libraries Apache Commons Lang 3.0, Apache log4j 1.2, iCal4j 1.0.4, and the 
Java/R Interface (JRI), which is part of rJava.

We also recommended (though by no means is it required) that you use an
Integrated Development Environment such as Eclipse (http://eclipse.org).
Especially if you are a Java novice, it will likely make your life much easier.

--MORE--

Additional information and documentation, a help forum, and more will be made
available on GitHub in the future.
