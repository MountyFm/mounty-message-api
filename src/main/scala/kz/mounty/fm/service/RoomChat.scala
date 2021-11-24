package kz.mounty.fm.service

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import kz.mounty.fm.domain.chat.RoomMessage

trait RoomChat {
  def chatFlow: Flow[RoomMessage, RoomMessage, Any]
}

object RoomChat {


  def create()(implicit system: ActorSystem): RoomChat = {
    val ((in, injectionQueue),out) =
      MergeHub.source[RoomMessage]
        .mergeMat(Source.queue[RoomMessage](100, OverflowStrategy.dropNew))(Keep.both)
        .toMat(BroadcastHub.sink[RoomMessage])(Keep.both)
        .run()


    val chatChannel: Flow[RoomMessage, RoomMessage, Any] = Flow.fromSinkAndSource(in, out)

    new RoomChat {

      override def chatFlow: Flow[RoomMessage, RoomMessage, Any] = {
        Flow[RoomMessage]
          .via(chatChannel)
      }

    }
  }

}