#!/bin/bash

# Down Migration Script
# This script rolls back database migrations in reverse order

set -e

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Database connection details
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-taskflow}
DB_USER=${DB_USER:-taskflow}
DB_PASSWORD=${DB_PASSWORD:-taskflow}

echo "Rolling back migrations..."

# Run down migrations in reverse order (V4 -> V3 -> V2 -> V1)
for version in 4 3 2 1; do
    migration_file="db-migrations-down/V${version}__*.down.sql"
    
    if ls $migration_file 1> /dev/null 2>&1; then
        echo "Rolling back V${version}..."
        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $migration_file
        echo "✓ V${version} rolled back"
    else
        echo "⚠ No down migration found for V${version}"
    fi
done

echo "All migrations rolled back successfully!"
