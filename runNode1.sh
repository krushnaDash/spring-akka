#!/usr/bin/env bash

set -x

mvn compile exec:exec -Dserver.port=8000 -Dakka.remote.artery.canonical.port=2551 -Dakka.management.http.port=8558 -Dcinnamon.prometheus.http-server.port=9001
