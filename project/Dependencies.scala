import sbt._

object Dependencies {
  lazy val `logback-classic` = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.2"
  lazy val `slack-api-client` = "com.slack.api" % "slack-api-client" % "1.0.5"
}
