#!/usr/bin/env bash

if [ "$1" = 'run' ]; then
    exec java -jar /var/lib/app/nfgp.jar
else
    exec "$@"
fi
