#!/bin/bash

source config.sh

for i in "${!LIB_NAMES[@]}"; do
    libName=${LIB_NAMES[$i]}
    libVersion=${LIB_VERSIONS[$i]}
    if [ -f $libName-$libVersion.aar ]; then
        mvn install:install-file -Dfile="$libName-$libVersion.aar" -DpomFile=pom-$libName.xml -DcreateChecksum=true
    else
        # A BOM is a POM with no AAR
        mvn install:install-file -Dfile=pom-$libName.xml -DpomFile=pom-$libName.xml -Dpackaging=pom -DcreateChecksum=true
    fi
done
