name := "classmates-test-task"

version := "0.1"

scalaVersion := "2.12.4"

val sparkV = "2.4.3"
val circeV = "0.13.0"
val enumeratumV = "1.6.1"

val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeV)

val enumeratumDeps = Seq(
  "com.beachape" %% "enumeratum",
  "com.beachape" %% "enumeratum-circe"
).map(_ % enumeratumV)

val sparkDeps = Seq(
  "org.apache.spark" %% "spark-core",
  "org.apache.spark" %% "spark-sql"
).map(_ % sparkV)

val testDeps = Seq(
  "org.scalatest" %% "scalatest" % "3.1.1"
).map(_ % "test")

libraryDependencies ++=
  sparkDeps ++
  circeDeps ++
  enumeratumDeps ++
  testDeps

resolvers += Resolver.sonatypeRepo("snapshots")

conflictManager := ConflictManager.latestRevision
