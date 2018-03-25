

# Workbench for Strings & Structures

To run the GUI-Version build the project and run `moduleworkbenchgui.jar`

```
mvn clean package
java -jar target/release/moduleworkbenchgui.jar
```

A command-line version of the tool is available after building as well.

```
java -jar target/release/modulebatchrunner.jar --help
```

Additionally, the modules contained in the workbench may be exported as standalone applications by running:

```
mvn package -Pstandalone
```

This produces a bunch of .jar files below `target/release`. Call with `--help` to get an overview of the options for each, e.g.:

```
java -jar target/release/SegmentMatrixModule.jar --help
```

## Web Server

A basic web server is available as well. It is meant to work with the [Benchly Workbench Coordinator](https://github.com/spinfo/benchly) but can be used on it's own.

A standalone jar of the server can be built with:

```
mvn compile assembly:single
```

Configuration is done via the command line or a `webserver.properties` file in the path.

```
java -jar target/modulewebserver-standalone.jar --help
```

To allow the encryption of storage locations, the environment variable BENCHLY_SHARED_SECRET needs to be set to the same value that the [coordinator](https://github.com/spinfo/benchly) uses.


![Alt text](http://www.spinfo.phil-fak.uni-koeln.de/sites/spinfo/_processed_/csm_UoC_Logo_mit_Excellent_Schriftzug_blau_19befdf80a.jpg)
