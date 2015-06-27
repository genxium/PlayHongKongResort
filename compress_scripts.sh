#!/bin/bash

# JS
PREFIX="./public/javascripts/"
COMMENT=$PREFIX/comment
ACTIVITY=$PREFIX/activity
ASSESSMENT=$PREFIX/assessment
USER=$PREFIX/user
HOMEPAGE=$PREFIX/homepage
WS=$PREFIX/ws
TARGET=$PREFIX/home.min.js
cat $PREFIX/lang_zh_hk.js $PREFIX/global_variables.js $PREFIX/global_functions.js $PREFIX/animations.js $PREFIX/models.js $PREFIX/participant.js > $TARGET
cat $COMMENT/viewer.js $COMMENT/editor.js >> $TARGET
cat $ASSESSMENT/editor.js $ASSESSMENT/viewer.js >> $TARGET
cat $PREFIX/notification.js >> $TARGET
cat $PREFIX/activity/functions.js $PREFIX/activity/editor.js $PREFIX/activity/detail.js >> $TARGET
cat $PREFIX/profile.js >> $TARGET
cat $USER/register.js $USER/login.js >> $TARGET
cat $PREFIX/topbar.js $PREFIX/footer.js $PREFIX/widgets.js >> $TARGET
cat $WS/websocket.js >> $TARGET
cat $HOMEPAGE/init.js >> $TARGET
java -jar yuicompressor-2.4.8.jar -o '.js$:.js' $TARGET 

ADMIN=$PREFIX/admin
TARGET=$PREFIX/admin.min.js
cat $PREFIX/lang_zh_hk.js $PREFIX/global_variables.js $PREFIX/global_functions.js $PREFIX/animations.js $PREFIX/models.js $PREFIX/participant.js > $TARGET
cat $COMMENT/viewer.js $COMMENT/editor.js >> $TARGET
cat $ASSESSMENT/editor.js $ASSESSMENT/viewer.js >> $TARGET
cat $PREFIX/notification.js >> $TARGET
cat $PREFIX/activity/functions.js $PREFIX/activity/editor.js $PREFIX/activity/detail.js >> $TARGET
cat $PREFIX/profile.js >> $TARGET
cat $USER/register.js $USER/login.js >> $TARGET
cat $PREFIX/topbar.js $PREFIX/footer.js $PREFIX/widgets.js >> $TARGET
cat $WS/websocket.js >> $TARGET
cat $ADMIN/init.js >> $TARGET
java -jar yuicompressor-2.4.8.jar -o '.js$:.js' $TARGET 

TARGET=$PREFIX/email_verification.min.js
cat $PREFIX/lang_zh_hk.js $PREFIX/global_variables.js $PREFIX/global_functions.js $PREFIX/animations.js $PREFIX/models.js $PREFIX/participant.js > $TARGET
cat $USER/email_verification.js >> $TARGET
java -jar yuicompressor-2.4.8.jar -o '.js$:.js' $TARGET 

TARGET=$PREFIX/password_index.min.js
cat $PREFIX/lang_zh_hk.js $PREFIX/global_variables.js $PREFIX/global_functions.js $PREFIX/animations.js $PREFIX/models.js $PREFIX/participant.js > $TARGET
cat $PREFIX/topbar.js $PREFIX/footer.js >> $TARGET
cat $USER/password_index.js >> $TARGET
java -jar yuicompressor-2.4.8.jar -o '.js$:.js' $TARGET 

TARGET=$PREFIX/password_reset.min.js
cat $PREFIX/lang_zh_hk.js $PREFIX/global_variables.js $PREFIX/global_functions.js $PREFIX/animations.js $PREFIX/models.js $PREFIX/participant.js > $TARGET
cat $USER/password_reset.js >> $TARGET
java -jar yuicompressor-2.4.8.jar -o '.js$:.js' $TARGET 

# LESS
ALL_LESS_FILES=$(find ./public/stylesheets -name "*.less" -type f)
LESS_FILE_RE='(.*)\.less'
for LESS_FILE in $ALL_LESS_FILES
do
	CSS_FILE="$(echo $LESS_FILE | sed 's/\(.*\)\.less/\1\.css/g')" # beware of the escaping  
	echo "$LESS_FILE > $CSS_FILE"
	lessc $LESS_FILE > $CSS_FILE
done


# CSS
CSS_PREFIX="./public/stylesheets"
TARGET_CSS=$CSS_PREFIX/theme-main.min.css
cat $CSS_PREFIX/theme-main.css > $TARGET_CSS
java -jar yuicompressor-2.4.8.jar -o '.css$:.css' $TARGET_CSS 

TARGET_CSS=$CSS_PREFIX/home.min.css
cat $CSS_PREFIX/common.css $CSS_PREFIX/header.css $CSS_PREFIX/footer.css $CSS_PREFIX/widgets.css $CSS_PREFIX/activity.css $CSS_PREFIX/assessment.css $CSS_PREFIX/comment.css $CSS_PREFIX/notification.css > $TARGET_CSS
cat $CSS_PREFIX/homepage.css >> $TARGET_CSS
java -jar yuicompressor-2.4.8.jar -o '.css$:.css' $TARGET_CSS 

TARGET_CSS=$CSS_PREFIX/admin.min.css
cat $CSS_PREFIX/common.css $CSS_PREFIX/header.css $CSS_PREFIX/footer.css $CSS_PREFIX/widgets.css $CSS_PREFIX/activity.css $CSS_PREFIX/assessment.css $CSS_PREFIX/comment.css $CSS_PREFIX/notification.css > $TARGET_CSS
cat $CSS_PREFIX/admin.css >> $TARGET_CSS
java -jar yuicompressor-2.4.8.jar -o '.css$:.css' $TARGET_CSS 

TARGET_CSS=$CSS_PREFIX/email_verification.min.css
cat $CSS_PREFIX/common.css $CSS_PREFIX/header.css $CSS_PREFIX/footer.css $CSS_PREFIX/widgets.css $CSS_PREFIX/activity.css $CSS_PREFIX/assessment.css $CSS_PREFIX/comment.css $CSS_PREFIX/notification.css > $TARGET_CSS
java -jar yuicompressor-2.4.8.jar -o '.css$:.css' $TARGET_CSS 

TARGET_CSS=$CSS_PREFIX/password_index.min.css
cat $CSS_PREFIX/common.css $CSS_PREFIX/header.css $CSS_PREFIX/footer.css $CSS_PREFIX/widgets.css $CSS_PREFIX/activity.css $CSS_PREFIX/assessment.css $CSS_PREFIX/comment.css $CSS_PREFIX/notification.css > $TARGET_CSS
cat $CSS_PREFIX/password.css >> $TARGET_CSS
java -jar yuicompressor-2.4.8.jar -o '.css$:.css' $TARGET_CSS 

TARGET_CSS=$CSS_PREFIX/password_reset.min.css
cat $CSS_PREFIX/common.css $CSS_PREFIX/header.css $CSS_PREFIX/footer.css $CSS_PREFIX/widgets.css $CSS_PREFIX/activity.css $CSS_PREFIX/assessment.css $CSS_PREFIX/comment.css $CSS_PREFIX/notification.css > $TARGET_CSS
cat $CSS_PREFIX/password.css >> $TARGET_CSS
java -jar yuicompressor-2.4.8.jar -o '.css$:.css' $TARGET_CSS 
