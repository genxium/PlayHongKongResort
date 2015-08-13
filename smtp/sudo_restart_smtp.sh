sudo postmap hash:/etc/postfix/stunnel_passwd
sudo postmap hash:/etc/postfix/sasl_passwd
sudo service postfix restart
# sudo rm /etc/postfix/stunnel_passwd
# sudo rm /etc/postfix/sasl_passwd

kill $(ps aux | grep 'stunnel' | awk '{print $2}')
sudo -uroot stunnel
