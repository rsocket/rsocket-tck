#!/bin/bash

if [ ! -z $1 ]; then
	java -cp target/scala-2.11/tck-assembly-*.jar io.reactivesocket.tck.$1
else
	echo "Usage: ./run <object name> [output location]"
fi

if [ ! -z $2 ]; then
    mv "$1".txt "$2"
fi
