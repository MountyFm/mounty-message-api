package kz.mounty.fm.api.dto

import kz.mounty.fm.domain.chat.RoomMessage
import org.joda.time.DateTime

import java.util.UUID

case class RoomMessageDTO(roomId: String,
                          profileId: String,
                          userName: String,
                          userAvatarUrl: Option[String] = None,
                          messageText: String)

object RoomMessageDTO{
  def convert(roomMessageDTO: RoomMessageDTO): RoomMessage = {
    RoomMessage(
      id = UUID.randomUUID().toString,
      roomId = roomMessageDTO.roomId,
      profileId = roomMessageDTO.profileId,
      userName = roomMessageDTO.userName,
      userAvatarUrl = roomMessageDTO.userAvatarUrl,
      messageText = roomMessageDTO.messageText,
      createdAt = new DateTime()
    )
  }

  def convert(roomMessage: RoomMessage): RoomMessageDTO = {
    RoomMessageDTO(
      roomId = roomMessage.roomId,
      profileId = roomMessage.profileId,
      userName = roomMessage.userName,
      userAvatarUrl = roomMessage.userAvatarUrl,
      messageText = roomMessage.messageText
    )
  }
}
