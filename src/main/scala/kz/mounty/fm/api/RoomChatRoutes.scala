package kz.mounty.fm.api

import akka.http.scaladsl.server.Directives._
import kz.mounty.fm.service.RoomChatService
import org.json4s.Formats
import org.json4s.native.Serialization._

import scala.concurrent.ExecutionContext

object RoomChatRoutes {
  def routes(implicit roomChatService: RoomChatService, ex: ExecutionContext, formats: Formats) =
    path("chat") {
      parameters(
        "roomId".as[String]) {
        (roomId) =>
          handleWebSocketMessages(roomChatService.handler(roomId))
      }
    } ~ path("messages") {
      parameters(
        "roomId".as[String],
        "numberOfMessages".as[Int]) { (roomId, numberOfMessages) =>
        onSuccess(roomChatService.findByRoomId(roomId,numberOfMessages)) {
          (response) => complete(write(response))
        }
      }
    }

}
