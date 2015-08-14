basedir=$(pwd)
sudo su - root -c "mysqldump -uroot --default-character-set=utf8 hongkongresort -r $basedir/all_data.sql"
