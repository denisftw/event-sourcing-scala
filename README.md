# Practical Event Sourcing with Scala

This is the companion code repository for the [Practical Event Sourcing with Scala](https://leanpub.com/modern-web-development-with-scala) book published by Leanpub. This is the second book in the series on Scala development. Other books are:

* [Modern Web Development with Scala](https://leanpub.com/modern-web-development-with-scala)
* [Mastering Advanced Scala](https://leanpub.com/mastering-advanced-scala)

## Description

The repository is split into chapters for easy reference. Each folder contains an independent project that could be compiled and run. Over the course of the book, we introduce new technologies and third-party libraries, so the project from chapter 7 is undoubtedly much more sophisticated than the project from chapter 1.

In fact, the project from chapter 1 is based on the Activator template. This template is loosely based on the sample project built in  [Modern Web Development with Scala](https://leanpub.com/modern-web-development-with-scala). The end project uses a lot of different technologies and third-party libraries. In particular, it uses:

* Play for implementing the backend
* MacWire for dependency injection
* ScalikeJDBC for accessing PostgreSQL
* Neo4J for storing the *read state*
* Kafka for implementing the data pipeline
* Webpack and Babel for enabling modern frontend workflow
* React, Redux, React Router for implementing the frontend
* Websockets for communicating with the frontend

Note that throughout the book we utilize different supporting technologies, and their number increases as we progress. For example, chapter 3 requires the Kafka server running, whereas chapter 4 also assumes that you have Neo4J running as well.

The frontend sources are separated from the backend and reside in the `ui` directory. All styles are included so that the application looks decent enough from the very beginning. The complete SASS source of Bootstrap (together with Glyphicons) is included for easy use.
