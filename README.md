# sql-functions

The `sql-functions` library provides functional interfaces for SQL operations. These are basically copies of the functional interfaces in [java.util.functions](http://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html) except their methods can throw [SQLExceptions](http://docs.oracle.com/javase/8/docs/api/java/sql/SQLException.html).

Each of these interfaces also contains static methods `unchecked` and `checked` to convert them into their matching JSE equivalents.
