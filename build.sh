#!/bin/bash 

cd DevappAndroid
./gradlew -p devapplib assembleRelease
if [ $? -ne 0 ]; then
    exit -1
fi

cp devapplib/build/outputs/aar/devapplib-release.aar ../com.ludei.devapp.android/libs/devapp/devapplib-release.aar
if [ $? -ne 0 ]; then
    exit -1
fi
cd -

