basedir=$(pwd)
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/create_table_user.sql"
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/create_table_activity.sql"
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/create_table_image.sql"
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/create_table_comment.sql"
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/create_table_assessment.sql"
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/create_table_login.sql"
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/create_table_notification.sql"
sudo su - root -c "mysql -uroot hongkongresort-devel < $basedir/create_table_user_activity_relation.sql"

