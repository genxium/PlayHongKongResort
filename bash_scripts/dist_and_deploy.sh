basedir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
sudo su - root -c "$basedir/../../play dist"
# always assume that /var/www/html is (over-)writable by the calling party
sudo su - root -c "unzip -o $basedir/../target/universal/hkr-1.0-SNAPSHOT.zip -d /var/www/html"
# kill the previous running daemon before starting the new one
sudo su - root -c "kill `ps aux|awk '/hkr-1.0-SNAPSHOT/ {print $2}'`"
# start new daemon in the background
sudo su - root -c "/var/www/html/hkr-1.0-SNAPSHOT/bin/hkr &"

