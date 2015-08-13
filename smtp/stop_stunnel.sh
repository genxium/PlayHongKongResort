kill $(ps aux | grep 'stunnel' | awk '{print $2}')
