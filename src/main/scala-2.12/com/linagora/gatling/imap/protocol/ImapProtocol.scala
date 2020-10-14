package com.linagora.gatling.imap.protocol

import java.util.Properties
import java.util.UUID.randomUUID

import akka.actor.ActorRef
import com.linagora.gatling.imap.protocol.Command.Disconnect
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

object ImapProtocol {
  val ImapProtocolKey = new ProtocolKey[ImapProtocol, ImapComponents] {

    var protocolNumber: Int = 0

    override def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[ImapProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): ImapProtocol = throw new IllegalStateException("Can't provide a default value for ImapProtocol")

    override def newComponents(coreComponents: CoreComponents): ImapProtocol => ImapComponents = { protocol =>
      val sessions: ActorRef = coreComponents.actorSystem.actorOf(ImapSessions.props(protocol), s"imapsessions-${protocolNumber}")
      protocolNumber += 1
      ImapComponents(protocol, sessions)
    }
  }
}

case class ImapComponents(protocol: ImapProtocol, sessions: ActorRef) extends ProtocolComponents {
  override def onStart: Session => Session = s => s

  override def onExit: Session => Unit = session => sessions ! Disconnect(UserId(session.userId))
}

case class ImapProtocol(host: String,
                        port: Int = 143,
                        config: Properties = new Properties()
                       ) extends Protocol
