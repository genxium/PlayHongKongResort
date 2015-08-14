basedir=$(pwd)

# install epel repo
sudo su - root -c "yum -y install epel-release"

# load CentOS-Base.repo
sudo su - root -c "cat $basedir/base_repo_template.conf > /etc/yum.repos.d/CentOS-Base.repo"

# install PostgreSQL RPM file
sudo su - root -c "yum -y localinstall http://yum.postgresql.org/9.4/redhat/rhel-6-x86_64/pgdg-centos94-9.4-1.noarch.rpm"

# install PostgreSQL 9.4
sudo su - root -c "yum -y install postgresql94-server"

# install npm
sudo su - root -c "yum -y install npm"

# install less via npm
sudo su - root -c "npm install -g less"

# install nginx by yum repo, reference: http://wiki.nginx.org/Install#Official_Red_Hat.2FCentOS_packages
sudo su - root -c "echo -e '[nginx]\nname=nginx repo\nbaseurl=http://nginx.org/packages/centos/$releasever/$basearch/\ngpgcheck=0\nenabled=1' > /etc/yum.repos.d/nginx.repo"
sudo su - root -c "yum -y install nginx"

# install mysql-server by yum repo, reference: http://dev.mysql.com/downloads/repo/yum/
sudo su - root -c "yum -y install wget"
sudo su - root -c "wget http://dev.mysql.com/get/mysql-community-release-el6-5.noarch.rpm"
sudo su - root -c "rpm -Uvh mysql-community-release-el6-5.noarch.rpm"
sudo su - root -c "yum -y install mysql-server"

# install system logging utility `sar`
sudo su - root -c "yum -y install sar"

# install jdk
sudo su - root -c "yum -y install java-1.7.0-openjdk-devel"

# install libplain.so & liblogin.so, reference http://www.postfix.org/SASL_README.html#client_sasl_policy
sudo su - root -c "yum -y install cyrus-sasl-plain"

# install SMTPS utility `stunnel`
sudo su - root -c "yum -y install stunnel"

# init `stunnel` configuration
sudo su - root -c "cat $basedir/../smtp/stunnel_template.conf > /etc/stunnel/stunnel.conf" 

# install mail utility `postfix`
sudo su - root -c "yum -y install yum-priorities"

# install up-to-date `postfix` 
sudo su - root -c "yum -y install postfix"

# init `postfix` configuration
sudo su - root -c "cat $basedir/../smtp/postfix_main_template.conf > /etc/postfix/main.cf" 

# install other tools
sudo su - root -c "yum -y install unzip libtool autoconf automake"
