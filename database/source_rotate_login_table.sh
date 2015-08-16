basedir=$(pwd)
mysql -uroot hongkongresort < $basedir/rotate_login_table.sql
