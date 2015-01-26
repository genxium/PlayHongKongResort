../play dist
# always assume that /var/www/html is (over-)writable by the calling party
unzip -o target/universal/hkr-1.0-SNAPSHOT.zip -d /var/www/html
# kill the previous running daemon before starting the new one
kill `ps aux|awk '/hkr-1.0-SNAPSHOT/ {print $2}'`
# start new daemon in the background
/var/www/html/hkr-1.0-SNAPSHOT/bin/hkr &

