basedir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
$basedir/../../play compile
$basedir/../../play stop
$basedir/../../play start
