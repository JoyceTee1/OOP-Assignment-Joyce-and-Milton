# SmartLib — CSP610 OOP Assignment

A Smart Library Management System built in Java 17 for the CSP610 Object-Oriented Programming module.

## How to build and run tests

```bash
mvn clean test
```

Coverage report is generated at `target/site/jacoco/index.html`.

## Project structure

```
src/main/java/smartlib/
    domain/       core entities and SOLID refactoring (Task 1)
    patterns/     Builder, Decorator, Observer (Task 2)
    generics/     generic Catalogue and type utilities (Task 3)
    functional/   Stream analytics and custom Collector (Task 4)
    concurrent/   async return pipeline, locks (Task 5)
    modern/       sealed types, records, pattern matching (Task 6)
src/test/java/    JUnit 5 tests
uml/              PlantUML diagram sources
doc/images/       UML diagram images
```

## Students

- 92604 Joyce Thomas
- 92607 Milton Jusu
