../play dist
# always assume that /var/www/html is (over-)writable by the calling party
unzip target/universal/hkr-1.0-SNAPSHOT.zip -o -d /var/www/html
/var/www/html/hkr-1.0-SNAPSHOT/bin/hkr
