basedir=$(pwd)
sudo su - root -c "mkdir -p /etc/nginx/vhost"
sudo su - root -c "cat $basedir/nginx_template.conf > /etc/nginx/nginx.conf"
sudo su - root -c "cat $basedir/nginx_api_template.conf > /etc/nginx/vhost/api.hongkongresort.com.conf"
