mkdir -p /etc/nginx/vhost
cat nginx_template.conf > /etc/nginx/nginx.conf
cat nginx_lb_template.conf > /etc/nginx/vhost/lb.hongkongresort.conf
