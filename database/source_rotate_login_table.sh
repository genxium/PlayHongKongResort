basedir=$(pwd)
sudo su - root -c "mysql -uroot hongkongresort < $basedir/rotate_login_table.sql"
