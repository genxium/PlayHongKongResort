basedir=$(pwd)
sudo su - root -c "mysql -uroot hongkongresort < $basedir/all_data.sql"
