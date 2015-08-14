# install data folder
mysql_install_db --user=mysql --ldata=/var/lib/mysql
sh mysqld_start.sh
mysql_secure_installation

cat mysqld_template.conf > /etc/my.cnf
