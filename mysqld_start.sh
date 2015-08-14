basedir=$(pwd)
sudo su - root -c "mysqld --user=mysql >$basedir/logs/mysqld.log 2>&1 &"
