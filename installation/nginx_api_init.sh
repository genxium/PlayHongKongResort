basedir=$(pwd)
sudo su - root -c "cat $basedir/nginx_api_template.conf > /etc/nginx/vhost/api.hongkongresort.com.conf"
