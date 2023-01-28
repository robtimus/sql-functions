# sql-functions
[![Maven Central](https://img.shields.io/maven-central/v/com.github.robtimus/sql-functions)](https://search.maven.org/artifact/com.github.robtimus/sql-functions)
[![Build Status](https://github.com/robtimus/sql-functions/actions/workflows/build.yml/badge.svg)](https://github.com/robtimus/sql-functions/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.robtimus%3Asql-functions&metric=alert_status)](https://sonarcloud.io/summary/overall?id=com.github.robtimus%3Asql-functions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.github.robtimus%3Asql-functions&metric=coverage)](https://sonarcloud.io/summary/overall?id=com.github.robtimus%3Asql-functions)
[![Known Vulnerabilities](https://snyk.io/test/github/robtimus/sql-functions/badge.svg)](https://snyk.io/test/github/robtimus/sql-functions)

The `sql-functions` library provides functional interfaces for SQL operations. These are basically copies of the functional interfaces in [java.util.functions](https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html) except their methods can throw [SQLExceptions](https://docs.oracle.com/javase/8/docs/api/java/sql/SQLException.html).

Each of these interfaces also contains static methods `unchecked` and `checked` to convert them into their matching JSE equivalents.
