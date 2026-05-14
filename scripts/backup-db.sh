#!/bin/bash

# Database backup script for AMS
# Usage: ./backup-db.sh [backup_dir]
# Default backup directory: /tmp/ams-backups

set -e

BACKUP_DIR="${1:-/tmp/ams-backups}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DB_NAME="${AMS_DB_NAME:-ams_db}"
DB_HOST="${AMS_DB_HOST:-localhost}"
DB_PORT="${AMS_DB_PORT:-3306}"
DB_USER="${AMS_DB_USER:-root}"
DB_PASS="${AMS_DB_PASSWORD:-root123}"

# Create backup directory if not exists
mkdir -p "$BACKUP_DIR"

BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.sql"

echo "Starting database backup..."
echo "Database: $DB_NAME@$DB_HOST:$DB_PORT"
echo "Backup file: $BACKUP_FILE"

# Perform mysqldump
mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
    --single-transaction \
    --quick \
    --lock-tables=false \
    --routines \
    --triggers \
    --events \
    "$DB_NAME" > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo "Backup completed successfully!"
    echo "File: $BACKUP_FILE (Size: $FILE_SIZE)"
    echo "$BACKUP_FILE"
    exit 0
else
    echo "Backup failed!"
    exit 1
fi
