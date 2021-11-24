package kz.mounty.fm.service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import kz.mounty.fm.api.dto.RoomMessageDTO
import kz.mounty.fm.domain.chat.RoomMessage
import kz.mounty.fm.repositories.RoomMessageRepository
import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization._
import org.mongodb.scala.MongoCollection

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class RoomChatService(implicit system: ActorSystem, formats: Formats, ex: ExecutionContext, collection: MongoCollection[RoomMessage]) {
  val chats: mutable.Map[String, RoomChat] = mutable.Map.empty[String, RoomChat]
  val roomMessageRepository = new RoomMessageRepository()

  def handler(roomId: String): Flow[Message, Message, Any] = {

    if(chats.keySet.contains(roomId)) {
      chatFlowByRoomId(chats(roomId))
    } else {
      val newChat = RoomChat.create()
      chats.put(roomId, newChat)
      chatFlowByRoomId(chats(roomId))
    }
  }

  def findByRoomId(roomId: String, n: Int):Future[Seq[RoomMessage]] = {
    roomMessageRepository.lastMessages(roomId, n)
  }

  def chatFlowByRoomId(chat: RoomChat): Flow[Message, Message, Any] = {
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) =>
          RoomMessageDTO.convert(parse(msg).extract[RoomMessageDTO])
      }
      .mapAsync[RoomMessage](2)((roomMessage: RoomMessage) => roomMessageRepository.save(roomMessage))
      .via(chat.chatFlow)
      .map {
        msg: RoomMessage =>
          TextMessage(Source.single(write(msg)))
      }
      .via(reportErrorsFlow)
  }

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ => // ignore regular completion
      })


}
