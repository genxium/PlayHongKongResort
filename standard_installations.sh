# install epel repo
yum install epel-release

# install npm
yum install npm

# install less via npm
npm install -g less

# install nginx by yum repo, reference: http://wiki.nginx.org/Install#Official_Red_Hat.2FCentOS_packages
echo -e '[nginx]\nname=nginx repo\nbaseurl=http://nginx.org/packages/centos/$releasever/$basearch/\ngpgcheck=0\nenabled=1' >> /etc/yum.repos.d/nginx.repo
yum install nginx

# install mysql-server by yum repo, reference: http://dev.mysql.com/downloads/repo/yum/
yum install wget
wget http://dev.mysql.com/get/mysql-community-release-el6-5.noarch.rpm
rpm -Uvh mysql-community-release-el6-5.noarch.rpm
yum install mysql-server

# install system logging utility `sar`
yum install sar

# install jdk
yum install java-1.7.0-openjdk-devel

# install vim editor
yum install vim

# install git
yum install git

# install SMTPS utility `stunnel`
yum install stunnel

# init `stunnel` configuration
cat smtp/stunnel_template.conf > /etc/stunnel/stunnel.conf 

# install mail utility `postfix`
yum install yum-priorities

# init CentOS-Base repo
cat base_repo_template.conf > /etc/yum.repos.d/CentOS-Base.repo

# install up-to-date `postfix` 
yum install postfix

# init `postfix` configuration
cat smtp/postfix_main_template.conf > /etc/postfix/main.cf 

# install other tools
yum install unzip libtool autoconf automake
