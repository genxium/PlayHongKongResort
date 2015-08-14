basedir=$(pwd)
sudo su - root -c "mysql_install_db --user=mysql --ldata=/var/lib/mysql"
sudo su - root -c "cat $basedir/mysqld_template.conf > /etc/my.cnf"
sudo su - root -c "sh $basedir/../mysqld_start.sh"
sudo su - root -c "mysql_secure_installation"
