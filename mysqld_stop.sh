kill $(ps aux | grep 'mysqld --user=mysql' | awk '{print $2}')
