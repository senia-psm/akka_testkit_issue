name := "akka_testKit_issue"
scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.5.18",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.5.18" % Test
)

libraryDependencies += "org.projectlombok" % "lombok" % "1.18.2" % Provided

resolvers in ThisBuild += Resolver.jcenterRepo

val junitVersion = "5.3.1"

testOptions += Tests.Argument(jupiterTestFramework, "-v")

libraryDependencies ++= Seq(
  "org.junit.jupiter" % "junit-jupiter-api" % junitVersion % Test,
  "org.junit.jupiter" % "junit-jupiter-params" % junitVersion % Test,
  "org.junit.jupiter" % "junit-jupiter-engine" % junitVersion % Test
)
