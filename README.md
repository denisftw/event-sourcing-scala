# Practical Event Sourcing with Scala

This is the companion code repository for the [Practical Event Sourcing with Scala](https://leanpub.com/modern-web-development-with-scala) book published by Leanpub. Being the second book in the series on Scala development, it picks up where the first one left off. Other books of the [Complete Scala Bundle](https://leanpub.com/b/complete-scala-bundle) are:

* [Modern Web Development with Scala](https://leanpub.com/modern-web-development-with-scala)
* [Mastering Advanced Scala](https://leanpub.com/mastering-advanced-scala)

## Description

The project is the final result that you will have if you follow along starting with the giter8 [template](https://github.com/denisftw/play-event-sourcing-starter.g8). That template is loosely based on the sample project built in  [Modern Web Development with Scala](https://leanpub.com/modern-web-development-with-scala) but adds several more things which are characteristic of a serious application. The end project uses a lot of different technologies and third-party libraries including:

* Play for implementing the backend
* MacWire for dependency injection
* ScalikeJDBC for accessing PostgreSQL
* Neo4J for storing the *read state*
* Webpack and Babel for enabling modern frontend workflow
* React, Redux, React Router for implementing the frontend
* Websockets for communicating with the frontend

The frontend sources are separated from the backend and reside in the `ui` directory. All styles are included so that the application looks decent enough from the very beginning.

## Running the application

Thanks to the magic of Docker Compose, the project is very easy to run. Essentially, all you need is three terminal windows: for Play, Webpack and Docker Compose.

To start Play, simply run `bash run-sbt.sh` in the root directory (of course, `sbt` and JDK must be already installed). Once `sbt` is initialized, type `run` to start the server on port `9000`.

Then, in a separate tab, first install NodeJS dependencies by running `npm install` (again, `npm` and `node` must be installed already). To make Webpack compile frontend resource in debug mode type `npm run watch`.

Finally, in the `stack` directory, type `docker-compose up` to start supporting services such as databases in their own containers.

The application will be up and running at [http://localhost:9000/](http://localhost:9000/). An existing admin user has login `user@example.com` with password `password123`. Once you're logged in, go to the [Admin](http://localhost:9000/admin) section and rewind the system to the current point in time. Once it's done, you should be able to add new tags, questions, answers etc. 