#!/bin/bash

# Script that finds and modifies java classes of modules that
# do not yet have a main class. The script also adds
# execution configurations to pom.xml for all modules modified.

# pom file
POMFILE="pom.xml"

# Check whether we are in the right folder to run this script
if [ ! -d src ]; then
  echo "I did not find the src subfolder. Please run this script from within the project folder."
  exit 1
fi

if [ ! -f "$POMFILE" ]; then
  echo "I did not find the pom.xml file. Please run this script from within the project folder."
  exit 1
fi

# Search for module classes without main
MODULE=$(find src/main/java/modules/ -name *.java|grep -v "/input_output/"|xargs -n 1 grep -l 'extends ModuleImpl'|xargs -n 1 grep -L 'public static void main')

# Define placeholder values
PH_MODULNAME="MODULNAME"
PH_MODULPFAD="MODULPFAD"

# Template for pom.xml additions
POMPART="\t\t\t\t\t\t\t<execution>\n\t\t\t\t\t\t\t\t<id>$PH_MODULNAME</id>\n\t\t\t\t\t\t\t\t<phase>package</phase>\n\t\t\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t\t\t<goal>jar</goal>\n\t\t\t\t\t\t\t\t</goals>\n\t\t\t\t\t\t\t\t<configuration>\n\t\t\t\t\t\t\t\t\t<outputDirectory>\n\t\t\t\t\t\t\t\t\t\t\${project.build.directory}/release\n\t\t\t\t\t\t\t\t\t</outputDirectory>\n\t\t\t\t\t\t\t\t\t<finalName>$PH_MODULNAME</finalName>\n\t\t\t\t\t\t\t\t\t<archive>\n\t\t\t\t\t\t\t\t\t\t<manifest>\n\t\t\t\t\t\t\t\t\t\t\t<addClasspath>true</addClasspath>\n\t\t\t\t\t\t\t\t\t\t\t<classpathPrefix>lib/</classpathPrefix>\n\t\t\t\t\t\t\t\t\t\t\t<mainClass>$PH_MODULPFAD$PH_MODULNAME</mainClass>\n\t\t\t\t\t\t\t\t\t\t</manifest>\n\t\t\t\t\t\t\t\t\t</archive>\n\t\t\t\t\t\t\t\t</configuration>\n\t\t\t\t\t\t\t</execution>"

# Process module classes
for i in $MODULE; do

  # Status output
  echo "$i"

  # Define module details
  MODNAME=$(echo "$i"|awk -F '/' '{ print $NF}'|sed -r s/"\.java$"//)
  MODPATH=$(echo "$i"|awk -F '/' '{for(i=4;i<NF;++i) print $i}'|tr '\n' '.')

  # Add a main method to the module class
  sed -i -r s/public\ +class\ +\([A-Za-z_0-9]+\)\ +extends\ +ModuleImpl\ +\\\{/"import base.workbench.ModuleRunner;\n\npublic class \1 extends ModuleImpl {\n\n\t\/\/ Main method for stand-alone execution\n\tpublic static void main(String[] args) throws Exception \{\n\t\tModuleRunner.runStandAlone(\1.class, args);\n\t\}\n"/ "$i"
  
  # Create an execution snippet to insert into pom.xml
  MODPOMPART=$(echo "$POMPART"|sed s/"$PH_MODULNAME"/"$MODNAME"/g|sed s/"$PH_MODULPFAD"/"$MODPATH"/g)

  # Determine the right place to insert the snippet
  INSERTLINE=$(grep -F -n '</executions>' "$POMFILE" | tail -n 1|awk -F ':' '{print $1}')
  INSERTLINE=$(($INSERTLINE-1))
  POMLINES=$(wc -l "$POMFILE"| awk '{print $1}')
  BOTTOM=$(($POMLINES-INSERTLINE))

  # Split pom.xml into head and tail section
  POMHEAD=$(head -n $INSERTLINE "$POMFILE")
  POMTAIL=$(tail -n $BOTTOM "$POMFILE")

  # Recombine head, addition and tail
  echo "$POMHEAD">"$POMFILE"
  echo -e "$MODPOMPART">>"$POMFILE"
  echo "$POMTAIL">>"$POMFILE"
done

