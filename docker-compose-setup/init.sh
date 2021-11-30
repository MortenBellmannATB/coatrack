#!/bin/sh

cd "$(dirname "$0")/.." || exit
mvn package -DskipTests
docker run --rm -dit -v coatrack-admin:/home --name coatrack-admin ubuntu
docker cp ./spring-boot/proxy/target/coatrack-proxy-2.0.0-SNAPSHOT.jar coatrack-admin:/home
docker stop coatrack-admin

docker run --rm -d -p 5432:5432 \
  --name coatrack-database \
  -v coatrack-database-volume:/var/lib/postgresql/data \
  -e POSTGRES_PASSWORD=password1234 postgres:9.4.26
sleep 5
docker exec coatrack-database psql -U postgres -c "create database coatrack;"
docker exec coatrack-database psql -U postgres -c "create database ygg_config_server;"
docker stop coatrack-database
