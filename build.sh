#!/bin/sh
set -e

: ${VERSION:="latest"}
export VERSION

chmod +x ./gradlew
./gradlew

docker build --build-arg VERSION=${VERSION} -t ecabs/ride-service:${VERSION} .
