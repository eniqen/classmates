package com.github.eniqen.classmates

import org.apache.spark.sql.{Encoder, Encoders, SparkSession}

import io.circe.parser._

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */

object StartApp extends App with SparkCodecs {

  final case class FeedInfo(
    userId: Long,
    platform: String,
    durationMs: Long,
    position: Int,
    timestamp: String,
    owners: Set[OwnerItem],
    resources: Set[ResourceItem]
  )

  final case class OwnerItem(id: Long, `type`: String)
  final case class ResourceItem(id: Long, `type`: String)

  // SCHEMA
  //  |-- userId: long (nullable = false)
  //  |-- platform: string (nullable = false)
  //  |-- durationMs: long (nullable = false)
  //  |-- position: integer (nullable = false)
  //  |-- timestamp: string (nullable = false)
  //  |-- owners: array (nullable = true)
  //  |    |    |-- id: long (nullable = false)
  //  |    |    |-- type: string (nullable = false)
  //  |-- resources: array (nullable = true)
  //  |    |    |-- id: long (nullable = false)
  //  |    |    |-- type: string (nullable = false)

  val spark = SparkSession.builder
    .appName("classmates-task")
    .master("local[*]")
    .getOrCreate
  import spark.sqlContext.implicits._

  spark.sparkContext.setLogLevel("ERROR")


  val encoder: Encoder[FeedInfo] = Encoders.product

  val lines = spark.sparkContext.textFile("src/main/resources/feeds_show.json")

  val df = lines.map(decode[FeedInfo])
    .collect { case Right(value) => value }
    .toDF

  df.createOrReplaceTempView("feeds")

  val userShowsPerDay = spark.sql(
    """
      | SELECT SUM(x2.login_times) user_times, x2.day
      | FROM (
      |   SELECT f.userId, COUNT(f.userId) login_times, to_date(f.timestamp,'yyyy-MM-dd') day, platform
      |   FROM feeds as f
      |   GROUP BY f.userId, day, f.platform
      | ) as x2
      | GROUP BY x2.day
    """.stripMargin)

  val userShowsPerPlatformAndDay = spark.sql(
    """
      | SELECT SUM(x2.login_times) user_times, x2.day, x2.platform
      | FROM (
      |   SELECT f.userId, COUNT(f.userId) login_times, to_date(f.timestamp,'yyyy-MM-dd') day, platform
      |   FROM feeds as f
      |   GROUP BY f.userId, day, f.platform
      | ) as x2
      | GROUP BY x2.day, x2.platform
      | ORDER BY x2.day, x2.platform
    """.stripMargin)

  userShowsPerDay.show
  userShowsPerPlatformAndDay.show
}
