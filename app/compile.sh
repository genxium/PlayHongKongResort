javac dao/ResultSetUtil.java -classpath ../lib/json-simple-1.1.1.jar:../lib/mysql-connector-java-5.1.26-bin.jar -Xlint;
javac dao/SQLHelper.java -classpath ../lib/json-simple-1.1.1.jar:../lib/mysql-connector-java-5.1.26-bin.jar:. -Xlint;
javac model/User.java -Xlint;
javac model/Post.java -Xlint;
javac utilities/Converter.java -Xlint;

sh $CATALINA_HOME/bin/shutdown.sh
sh $CATALINA_HOME/bin/startup.sh
