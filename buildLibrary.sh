#!/bin/bash

cd /Users/sbillings/Documents/git/hub-docker-inspector
pwd

pushd build/classes/groovy/main
jar cvf ../../../../hub-docker-inspector-4.3.2-SNAPSHOT.jar com
popd
pwd
mvn install:install-file -Dfile=./hub-docker-inspector-4.3.2-SNAPSHOT.jar -DgroupId=com.blackducksoftware.integration -DartifactId=image-inspector -Dversion=4.3.2-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
