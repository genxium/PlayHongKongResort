basedir=$(pwd)
sudo su - root -c "mysqldump -uroot -d hongkongresort activity > $basedir/create_table_activity.sql"
sudo su - root -c "mysqldump -uroot -d hongkongresort assessment > $basedir/create_table_assessment.sql"
sudo su - root -c "mysqldump -uroot -d hongkongresort comment > $basedir/create_table_comment.sql"
sudo su - root -c "mysqldump -uroot -d hongkongresort image > $basedir/create_table_image.sql"
sudo su - root -c "mysqldump -uroot -d hongkongresort login > $basedir/create_table_login.sql"
sudo su - root -c "mysqldump -uroot -d hongkongresort notification > $basedir/create_table_notification.sql" 
sudo su - root -c "mysqldump -uroot -d hongkongresort user_activity_relation > $basedir/create_table_user_activity_relation.sql"
sudo su - root -c "mysqldump -uroot -d hongkongresort perm_foreign_party > $basedir/create_table_perm_foreign_party.sql"
sudo su - root -c "mysqldump -uroot -d hongkongresort temp_foreign_party > $basedir/create_table_temp_foreign_party.sql"
sudo su - root -c "mysqldump -uroot hongkongresort user > $basedir/create_table_user.sql"
 

