#!/bin/bash

set -e
set -u

DB_NAME="sakila"
DB_USER="postgres"
DB_PASSWORD="postgres"
DB_PORT="5432"
DB_CONTAINER_NAME="sakila"

SQL_DUMP_FILE="src/integration-test/resources/full_dump.sql"

echo "🐳 Stopping any existing PostgreSQL container..."
docker stop $DB_CONTAINER_NAME >/dev/null 2>&1 || true
docker rm $DB_CONTAINER_NAME >/dev/null 2>&1 || true

echo "🚀 Starting PostgreSQL container..."
docker run -d \
    --name $DB_CONTAINER_NAME \
    -e POSTGRES_USER=$DB_USER \
    -e POSTGRES_PASSWORD=$DB_PASSWORD \
    -e POSTGRES_DB=$DB_NAME \
    -p $DB_PORT:5432 \
    postgres:15

echo "⏳ Waiting for PostgreSQL to be fully ready..."
until docker exec $DB_CONTAINER_NAME pg_isready -U $DB_USER -d $DB_NAME >/dev/null 2>&1; do
    echo "Waiting for database to start..."
    sleep 2
done

echo "✅ Database is ready!"

if [ ! -f "$SQL_DUMP_FILE" ]; then
    echo "❌ SQL dump file not found: $SQL_DUMP_FILE"
    exit 1
fi

echo "📥 Loading data from $SQL_DUMP_FILE..."
docker exec -i $DB_CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < "$SQL_DUMP_FILE"

echo "✅ Database setup is complete!"
