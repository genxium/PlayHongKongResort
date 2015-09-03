name := "HKR"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-dbcp2" % "2.1.1",
  "org.apache.commons" % "commons-pool2" % "2.4.2",
  "com.github.penggle" % "kaptcha" % "2.3.2",
  "com.sun.mail" % "javax.mail" % "1.5.4",
  "com.qiniu" % "qiniu-java-sdk" % "7.0.4.1",
  "org.mockito" % "mockito-core" % "1.10.19"
)

play.Project.playJavaSettings
