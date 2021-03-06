package me.jeffshaw.digitalocean.metadata

import me.jeffshaw.digitalocean.RegionEnum
import me.jeffshaw.digitalocean.ToFuture._
import org.asynchttpclient.{AsyncHttpClient, RequestBuilder}
import org.json4s.Extraction
import org.json4s.native.JsonMethods
import scala.concurrent.{ExecutionContext, Future}

case class Metadata(
  dropletId: BigInt,
  hostname: String,
  vendorData: String,
  publicKeys: Seq[String],
  region: RegionEnum,
  interfaces: Interfaces,
  floatingIp: Option[FloatingIp],
  dns: Dns
)

object Metadata {
  import me.jeffshaw.digitalocean.formats

  def apply(metadata: String): Metadata = {
    val json = JsonMethods.parse(metadata)
    Extraction.extract[responses.Metadata](json).toMetadata
  }

  def apply()(implicit client: AsyncHttpClient, ec: ExecutionContext): Future[Metadata] = {
    for {
      response <- client.executeRequest(endpoint)
    } yield Metadata(response.getResponseBody)
  }

  val endpoint = new RequestBuilder("GET").setUrl("http://169.254.169.254/metadata/v1.json").build()
}
