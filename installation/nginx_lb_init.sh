basedir=$(pwd)
sudo su - root -c "cat $basedir/nginx_lb_template.conf > /etc/nginx/vhost/lb.hongkongresort.conf"
