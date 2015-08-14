basedir=$(pwd)
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/rotate_login_table.sql"
