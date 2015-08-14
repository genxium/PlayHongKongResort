# install epel repo
yum -y install epel-release

# load CentOS-Base.repo
cat base_repo_template.conf > /etc/yum.repos.d/CentOS-Base.repo

# install PostgreSQL RPM file
yum -y localinstall http://yum.postgresql.org/9.4/redhat/rhel-6-x86_64/pgdg-centos94-9.4-1.noarch.rpm

# install PostgreSQL 9.4
yum -y install postgresql94-server

# install npm
yum -y install npm

# install less via npm
npm install -g less

# install nginx by yum repo, reference: http://wiki.nginx.org/Install#Official_Red_Hat.2FCentOS_packages
echo -e '[nginx]\nname=nginx repo\nbaseurl=http://nginx.org/packages/centos/$releasever/$basearch/\ngpgcheck=0\nenabled=1' > /etc/yum.repos.d/nginx.repo
yum -y install nginx

# install mysql-server by yum repo, reference: http://dev.mysql.com/downloads/repo/yum/
yum -y install wget
wget http://dev.mysql.com/get/mysql-community-release-el6-5.noarch.rpm
rpm -Uvh mysql-community-release-el6-5.noarch.rpm
yum -y install mysql-server

# install system logging utility `sar`
yum -y install sar

# install jdk
yum -y install java-1.7.0-openjdk-devel

# install vim editor
yum -y install vim

# install git
yum -y install git

# install libplain.so & liblogin.so, reference http://www.postfix.org/SASL_README.html#client_sasl_policy
yum -y install cyrus-sasl-plain

# install SMTPS utility `stunnel`
yum -y install stunnel

# init `stunnel` configuration
cat smtp/stunnel_template.conf > /etc/stunnel/stunnel.conf 

# install mail utility `postfix`
yum -y install yum-priorities

# install up-to-date `postfix` 
yum -y install postfix

# init `postfix` configuration
cat smtp/postfix_main_template.conf > /etc/postfix/main.cf 

# install other tools
yum -y install unzip libtool autoconf automake
