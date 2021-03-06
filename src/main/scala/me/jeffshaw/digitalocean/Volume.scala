package me.jeffshaw.digitalocean

import java.time.Instant
import org.json4s.JValue
import org.json4s.JsonDSL._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class Volume(
  id: String,
  region: Region,
  dropletIds: Seq[BigInt],
  name: String,
  description: Option[String],
  sizeGigabytes: Int,
  createdAt: Instant
) {

  def exists()(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Boolean] = {
    Volume.exists(id)
  }

  def actions()(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Iterator[Action]] = {
    Volume.actions(id)
  }

  def attach(dropletId: BigInt)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Action] = {
    Volume.attach(id, dropletId, region.slug)
  }

  def delete()(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[VolumeDeletion] = {
    Volume.delete(id)
  }

  def isDeleted()(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Boolean] = {
    Volume.isDeleted(id)
  }

  def detach(dropletId: BigInt)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Action] = {
    Volume.detach(id, dropletId, region.slug)
  }

  def detachAll()(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Seq[Action]] = {
    Future.sequence(dropletIds.map(detach))
  }

  def resize(newSizeGigabytes: Int)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Action] = {
    Volume.resize(id, newSizeGigabytes, region)
  }

}

object Volume
  extends Path
    with Listable[Volume, responses.Volumes] {

  override val path: Seq[String] = Seq("volumes")

  def apply(id: String)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Volume] = {
    for {
      response <- client.get[responses.Volume](path :+ id)
    } yield {
      response.volume
    }
  }

  def byName(name: String, region: RegionEnum)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Volume] = {
    val parameters = Map(
      "name" -> Seq(name),
      "region" -> Seq(region.slug)
    )
    for {
      response <- client.get[responses.Volume](path, parameters)
    } yield response.volume
  }

  def create(sizeGigabytes: Int, name: String, description: Option[String], region: RegionEnum)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Volume] = {
    val message: JValue =
      ("size_gigabytes" -> sizeGigabytes) ~
        ("name" -> name) ~
        ("description" -> description) ~
        ("region" -> region.slug)
    for {
      response <- client.post[responses.Volume](path, message)
    } yield response.volume
  }

  def exists(id: String)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Boolean] = {
    val path = this.path :+ id
    client.exists(path)
  }

  def isDeleted(id: String)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Boolean] = {
    for {
      exists <- exists(id)
    } yield {! exists}
  }

  def delete(id: String)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[VolumeDeletion] = {
    for {
      () <- client.delete(path :+ id)
    } yield VolumeDeletion(id)
  }

  def deleteByName(name: String, region: RegionEnum)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Unit] = {
    val parameters = Map(
      "name" -> Seq(name),
      "region" -> Seq(region.slug)
    )
    client.delete(path, queryParameters = parameters)
  }

  def attach(id: String, dropletId: BigInt, region: RegionEnum)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Action] = {
    val message: JValue =
      ("type" -> "attach") ~
        ("droplet_id" -> dropletId) ~
        ("region" -> region.slug)

    for {
      response <- client.post[responses.Action](path :+ id :+ "actions", message)
    } yield response.action
  }

  def attachByName(name: String, dropletId: BigInt, region: RegionEnum)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Action] = {
    val message: JValue =
      ("type" -> "attach") ~
        ("droplet_id" -> dropletId) ~
        ("volume_name" -> name) ~
        ("region" -> region.slug)

    for {
      response <- client.post[responses.Action](path :+ "actions", message)
    } yield response.action
  }

  def detach(id: String, dropletId: BigInt, region: RegionEnum)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Action] = {
    val message: JValue =
      ("type" -> "detach") ~
        ("droplet_id" -> dropletId) ~
        ("region" -> region.slug)

    for {
      response <- client.post[responses.Action](path :+ id :+ "actions", message)
    } yield response.action
  }

  def detachByName(name: String, dropletId: BigInt, region: RegionEnum)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Action] = {
    val message: JValue =
      ("type" -> "detach") ~
        ("droplet_id" -> dropletId) ~
        ("volume_name" -> name) ~
        ("region" -> region.slug)

    for {
      response <- client.post[responses.Action](path :+ "actions", message)
    } yield response.action
  }

  def actions(id: String)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Iterator[Action]] = {
    Listable.listGet[Action, responses.Actions](path :+ id :+ "actions")
  }

  def resize(id: String, newSizeGigabytes: Int, region: RegionEnum)(implicit client: DigitalOceanClient, ec: ExecutionContext): Future[Action] = {
    val message: JValue =
      ("type" -> "resize") ~
        ("size_gigabytes" -> newSizeGigabytes) ~
        ("region" -> region.slug)

    client.post[responses.Action](path :+ id :+ "actions", message).map(_.action)
  }

}
