basedir=$(pwd)
sudo su - root -c "mkdir -p /etc/nginx/vhost"
sudo su - root -c "cat $basedir/nginx_template.conf > /etc/nginx/nginx.conf"
sudo su - root -c "cat $basedir/nginx_lb_template.conf > /etc/nginx/vhost/lb.hongkongresort.conf"
