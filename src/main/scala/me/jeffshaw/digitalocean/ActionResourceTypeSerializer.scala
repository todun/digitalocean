package me.jeffshaw.digitalocean

import org.json4s._

private[digitalocean] case object ActionResourceTypeSerializer extends CustomSerializer[Action.ResourceType](format =>
  (
    {
      case JString("droplet") =>
        Action.Droplet
      case JString("backend") =>
        Action.Backend
      case JString("image") =>
        Action.Image
      case JString("floating_ip") =>
        Action.FloatingIp
    },
    {
      case Action.Droplet =>
        JString("droplet")
      case Action.Backend =>
        JString("backend")
      case Action.Image =>
        JString("image")
      case Action.FloatingIp =>
        JString("floating_ip")
    }
  )
)
