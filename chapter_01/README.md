# Practical Event Sourcing with Scala

This is the activator template of sample event sourcing application that we will be building over the course of the [Practical Event Sourcing with Scala](https://leanpub.com/practical-event-sourcing-with-scala) ebook I'm currently writing.

## Description

The template is loosely based on the sample application built during the course of [Modern Web Development with Scala](https://leanpub.com/modern-web-development-with-scala). In particular:

* The dependency injection is based on MacWire
* A simple authentication mechanism is based on cookies and Play API
* A database connectivity module is based on ScalikeJDBC and uses PostgreSQL

The frontend sources are separated from the backend and reside in the `ui` directory. All necessary dependencies are already included in the `package.json`. These include:

* Webpack as the frontend build tool/module bundler
* Babel transpiler for using EcmaScript 6 (arrows, classes, `let`, `const`)
* React, React Router and Redux
* Axios as the frontend HTTP client

All styles are included so that the application looks decent enough from the very beginning. The complete SASS source of Bootstrap (together with Glyphicons) is included for easy use.

## Getting started

Since this template is based on Activator, I assume that you already have JDK8 installed.
  In addition, you will also need to have PostgreSQL 9.5 installed to run the app. If you're using an up-to-date version of Ubuntu, this is easy to do:

  ```
  $ sudo apt-get install postgresql-9.5 postgresql-contrib-9.5 pgadmin3
  ```

  Make sure that the PostgreSQL server is up and running and create a new user:

  ```
  $ /usr/bin/sudo -u postgres psql --command "CREATE USER scalauser WITH SUPERUSER PASSWORD 'scalapass';"
  ```

  Then, create a new database for our authentication mechanism:

  ```
  $ /usr/bin/sudo -u postgres createdb -O scalauser authdb
  ```

  The application also utilizes a frontend workflow based on NodeJS. This means that you'll need to have two NodeJS tools installed - `node` and `npm`. Since NodeJS is in constant development, I recommend you install nvm - the NodeJS version manager. Just go to <a href="https://github.com/creationix/nvm#install-script">their website</a> and follow the instructions. Again, if you're on Ubuntu (or other Unix), everything is easy:

  ```
  $ curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.32.1/install.sh | bash
  ```

  After `nvm` is installed, use it to obtain NodeJS itself:

  ```
  $ nvm install v5.3.0
  ```

  Add this to the very end of your `.bashrc` to make sure that NodeJS 5.3.0 is used by default:

  ```
  nvm use v5.3.0 > /dev/null
  ```

  Open a new terminal and check that `npm` is available:

  ```
  $ npm --version
  3.3.12
  ```

  Finally, go to the project directory and download frontend dependencies by typing the following:

  ```
  $ npm install
  ```

  `npm` will download necessary packages to `node_modules` (not under version control). It may take some time, but once it is done, you can finally start compilation of frontend assets by invoking the following script:

  ```
  $ npm run watch
  ```

  Finally, use another terminal window to start Activator in debug mode:

  ```
  $ ./activator -jvm-debug 9999
  ```

  Note that Webpack is started in the monitor mode and recompile frontend assets after you change them. Once you start the Play application from Activator using the `run` command, it will also start monitoring backend source files for changes.


## Versions used

The code uses Play 2.5 and Scala 2.11.8 along with sbt 0.13.11.
