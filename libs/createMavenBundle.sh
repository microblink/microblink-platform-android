#!/bin/bash

source config.sh

for i in "${!LIB_NAMES[@]}"; do
    libName=${LIB_NAMES[$i]}
    libVersion=${LIB_VERSIONS[$i]}

    tmpDir=./tmp-$libName
    tmpDest=$tmpDir/com/microblink/$libName/$libVersion

    mkdir -p $tmpDest
    cp pom-$libName.xml $tmpDest/$libName-$libVersion.pom
    files=($libName-$libVersion.pom)
    # A BOM is a POM with no AAR
    if [ -f $libName-$libVersion.aar ]; then
        cp $libName-$libVersion.aar $tmpDest/$libName-$libVersion.aar
        files+=($libName-$libVersion.aar)
    fi

    pushd $tmpDest > /dev/null
    for file in "${files[@]}"; do
        md5 -q $file > $file.md5
        shasum $file | cut -d ' ' -f 1 > $file.sha1
        gpg -ab $file
    done
    popd > /dev/null

    (cd $tmpDir && zip -r ../$libName-$libVersion-maven-bundle.zip .)
    rm -r $tmpDir
done
