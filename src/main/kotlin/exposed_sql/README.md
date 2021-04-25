
# Exposed Intellij SQL Framework

Useful docs and articles:
* The github Exposed 
<a href="https://github.com/JetBrains/Exposed/wiki">
wiki</a>
* Oracle
  <a href="https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html">
  JDBC Tutorial</a>
* Baeldung:
<a href="https://www.baeldung.com/kotlin/exposed-persistence">
  Guide to the Kotlin Exposed Framework</a>

Fri Apr 23 13:47:38 PDT 2021: Installed:
org.jetbrains.exposed:exposed:0.17.13<br>
org.jetbrains.exposed:exposed-core:0.30.2<br>
org.jetbrains.exposed:exposed-dao:0.30.2<br>
org.jetbrains.exposed:exposed-jdbc:0.30.2<br>


Misc:<br>
Exposed supports both
<a href="https://github.com/JetBrains/Exposed/wiki/DSL">
  DSL</a> and <a href="https://github.com/JetBrains/Exposed/wiki/DAO">
  DAO</a> syntax.

Notes:<br>
Seemed to work for h2 dialact but failed for sqlite with message:<br>
> SQLite supports only TRANSACTION_SERIALIZABLE and TRANSACTION_READ_UNCOMMITTED.

Eventually found example on
<a href="https://titanwolf.org/Network/Articles/Article?AID=bc808686-8b97-4562-b0a5-4391a20eef0d">
Titanwolf.org</a>.  In his esample the transaction statement looked like:
> transaction(transactionIsolation = Connection.TRANSACTION_SERIALIZABLE, repetitionAttempts = 3) {

That seems to work.

---
Acronyms, abbreviations, definations, etc.<br>
CRUD: Acronym for Create, Read, Update<br>
ORM: Acronym for Object-Relational Mapping<br>