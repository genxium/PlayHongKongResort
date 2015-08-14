basedir=$(pwd)
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/all_data.sql"
