package kz.mounty.fm.repositories

import kz.mounty.fm.domain.chat.RoomMessage
import kz.mounty.fm.repository.BaseRepository
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Sorts.{ascending, descending}

import scala.concurrent.{ExecutionContext, Future}

class RoomMessageRepository(implicit ec: ExecutionContext, collection: MongoCollection[RoomMessage]) extends BaseRepository{
  def findByRoomId(roomId: String): Future[Option[RoomMessage]] =
    findByFilter[RoomMessage](equal("roomId", roomId))

  def lastMessages(roomId: String, n: Int): Future[Seq[RoomMessage]] =
    collection
      .find(equal("roomId", roomId))
      .sort(descending("createdAt"))
      .limit(n)
      .collect()
      .head()
}
