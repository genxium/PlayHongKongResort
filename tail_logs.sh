basedir=$(pwd)
sudo su - root -c "tail -f $basedir/logs/application.log /var/log/maillog /var/log/stunnel.log /var/log/messages"
