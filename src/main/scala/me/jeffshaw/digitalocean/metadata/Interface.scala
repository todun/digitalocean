package me.jeffshaw.digitalocean.metadata

import me.jeffshaw.digitalocean.NetworkType

case class Interface(
  ipv4: Option[Ipv4],
  anchorIpv4: Option[Ipv4],
  ipv6: Option[Ipv6],
  anchorIpv6: Option[Ipv6],
  mac: String,
  `type`: NetworkType
)
