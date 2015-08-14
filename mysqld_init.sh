# install data folder
mysql_install_db --user=mysql --ldata=/var/lib/mysql
mysql_secure_installation

cat mysqld_template.conf > /etc/my.cnf
