#!/bin/sh

set -eu

PORT="${PORT:-8080}"

sed -i -E "s/(<Connector port=\")[0-9]+(\")/\1${PORT}\2/" /usr/local/tomcat/conf/server.xml

echo "Starting Rahul Mart on port ${PORT}"

exec /usr/local/tomcat/bin/catalina.sh run
