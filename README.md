

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
mvn clean package -Pstandalone
```

This produces a bunch of .jar files below `target/release`. Call with `--help` to get an overview of the options for each, e.g.:

```
java -jar target/release/SegmentMatrixModule.jar --help
```


![Alt text](http://www.spinfo.phil-fak.uni-koeln.de/sites/spinfo/_processed_/csm_UoC_Logo_mit_Excellent_Schriftzug_blau_19befdf80a.jpg)
