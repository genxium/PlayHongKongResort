# install data folder
mysql_install_db --user=root --ldata=/var/lib/mysql

# sudo permission for group `wheel` is assumed
chown root:wheel -R /var/lib/mysql
chmod 775 -R /var/lib/mysql

# need manual confirmation for options
mysql_secure_installation

cat mysqld_template.conf > /etc/my.cnf
