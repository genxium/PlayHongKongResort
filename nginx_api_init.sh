mkdir -p /etc/nginx/vhost
cat nginx_template.conf > /etc/nginx/nginx.conf
cat nginx_api_template.conf > /etc/nginx/vhost/api.hongkongresort.com.conf
