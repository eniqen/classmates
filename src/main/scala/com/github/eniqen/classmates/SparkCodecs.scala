package com.github.eniqen.classmates

import com.github.eniqen.classmates.StartApp.{FeedInfo, OwnerItem, ResourceItem}
import io.circe.{Decoder, Json}

import scala.util.control.TailCalls.{TailRec, done, tailcall}

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */

trait FeedInfoCodecs {
  implicit val decoderFeed: Decoder[FeedInfo] = Decoder.instance {
    c =>
      for {
        id  <- c.get[Long]("userId")
        pl  <- c.get[String]("platform")
        dur <- c.get[Long]("durationMs")
        pos <- c.get[Int]("position")
        ts  <- c.get[Long]("timestamp").map(_.formatTs)

        owners_user = c.downField("owners")
          .get[Set[Long]]("user")
          .toOption
          .map(_.map(OwnerItem(_, "user")))
          .getOrElse(Set.empty)

        owners_group = c.downField("owners")
          .get[Set[Long]]("group")
          .toOption
          .map(_.map(OwnerItem(_, "group")))
          .getOrElse(Set.empty)

        resources = getResources(c.downField("resources").focus.getOrElse(Json.Null))
      } yield FeedInfo(id, pl, dur, pos, ts, owners_group union owners_user, resources)
  }

  private def getResources(json: Json): Set[ResourceItem] = {
    def tailRecUncons(d: Vector[Json], acc: Set[ResourceItem]): TailRec[Set[ResourceItem]] = d.headOption.fold(done(acc)) {
      _.asObject.fold(done(acc)) {
        jsonObj =>
          val parsedResult = jsonObj.toVector.foldLeft(acc) {
            case (res, (key, idsJson)) =>
              idsJson.as[Set[Long]].toOption.map {
                _.map(ResourceItem(_, key))
              }.fold(res)(_ union res)
          }
          tailcall(tailRecUncons(d.tail, parsedResult))
      }
    }
    tailRecUncons(Vector(json), Set.empty[ResourceItem]).result
  }
}

trait SparkCodecs extends FeedInfoCodecs

object SparkCodecs extends SparkCodecs