
# Sqlite console demo

I needed a simple console application using java.sql
and written in Kotlin to help with evaluation of 
different sql frameworks.  I want to look at Intellij's
Exposed Framework for Kotlin and needed to understand 
some basics.

This is the result.  It started life as a basic 
tutorial from 
<a href="https://www.tutorialspoint.com/sqlite/sqlite_java.htm">
Tutorialspoint</a>
and that I modified to Kotlin and will evolve from there.

The version under java_sql uses only the Java sql library.

The java sql driver was downloaded from Maven:<br>
  org.xerial:sqlite-jdbc:3.34.0
  

Tue Apr 27 11:29:24 PDT 2021<br>
added some simple demo stuff using IntelliJ's Exposed 
SQL Framework.  There are two simple demos:one using DSL
and the other using the DAO model.  They are using
Sqlite.
