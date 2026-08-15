DB_NAME="smart_meeting"
BACKUP_DIR="/data/backup"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR
mysqldump -h127.0.0.1 -uroot -p$MYSQL_ROOT_PASSWORD $DB_NAME > $BACKUP_DIR/${DB_NAME}_${DATE}.sql
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete