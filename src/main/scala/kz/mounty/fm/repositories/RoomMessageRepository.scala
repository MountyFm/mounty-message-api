package kz.mounty.fm.repositories

import kz.mounty.fm.domain.chat.RoomMessage
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Sorts.ascending

import scala.concurrent.{ExecutionContext, Future}

class RoomMessageRepository(implicit ec: ExecutionContext, collection: MongoCollection[RoomMessage]){
  def findByRoomId(roomId: String): Future[Option[RoomMessage]] = {
    collection
      .find(Document("roomId" -> roomId))
      .first()
      .head
      .map(Option(_))
  }

  def save(roomMessage: RoomMessage): Future[RoomMessage] =
    collection
      .insertOne(roomMessage)
      .head()
      .map(_ => roomMessage)


  def lastMessages(roomId: String, n: Int): Future[Seq[RoomMessage]] =
    collection
      .find(Document("roomId" -> roomId))
      .sort(ascending("createdAt"))
      .limit(n)
      .collect()
      .head()

  def all: Future[Seq[RoomMessage]] =
    collection
      .find()
      .collect()
      .head()
}
