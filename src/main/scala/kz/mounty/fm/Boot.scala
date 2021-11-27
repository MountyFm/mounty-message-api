package kz.mounty.fm

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.util.{ByteString, Timeout}
import com.typesafe.config.{Config, ConfigFactory}
import kz.mounty.fm.api.RoomChatRoutes
import kz.mounty.fm.domain.chat.RoomMessage
import kz.mounty.fm.serializers.Serializers
import kz.mounty.fm.service.RoomChatService
import kz.mounty.fm.util.JodaCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext

object Boot extends App with Serializers{
  implicit val config: Config = ConfigFactory.load()
  implicit val system = ActorSystem("mounty-message-api")
  implicit val mat = Materializer(system)
  implicit val ex: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  val codecRegistry = fromRegistries(
    fromProviders(classOf[RoomMessage]),
    CodecRegistries.fromCodecs(new JodaCodec),
    DEFAULT_CODEC_REGISTRY)

  val client: MongoClient = MongoClient(config.getString("mongo.url"))
  val db: MongoDatabase = client
    .getDatabase(config.getString("mongo.db"))
    .withCodecRegistry(codecRegistry)
  implicit val collection: MongoCollection[RoomMessage] = db
    .getCollection[RoomMessage](config.getString("mongo.collection"))

  implicit val roomChatService: RoomChatService = new RoomChatService()

  val pingCounter = new AtomicInteger()

  val bindingFuture = Http()
    .newServerAt("localhost", 8080)
    .adaptSettings(_.mapWebsocketSettings(_.withPeriodicKeepAliveData(() => ByteString(s"debug-${pingCounter.incrementAndGet()}"))))
    .bind(RoomChatRoutes.routes)


}

