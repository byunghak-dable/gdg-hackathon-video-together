package org.personal.videotogether.server.entity

import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class ChatRoomMapper
@Inject
constructor(
    private val userMapper: UserMapper
) : EntityMapper<ChatRoomEntity, ChatRoomData> {
    override fun mapFromEntity(entity: ChatRoomEntity): ChatRoomData {
        return ChatRoomData(
            id = entity.id,
            lastChatMessage = entity.lastChatMessage,
            lastChatTime = entity.lastChatTime,
            participantList = userMapper.mapFromEntityList(entity.participantList),
            unReadChatCount = entity.un_read_chat_count,
            isSelected = false
        )
    }

    override fun mapToEntity(domainModel: ChatRoomData): ChatRoomEntity {
        return ChatRoomEntity(
            id = domainModel.id,
            lastChatMessage = domainModel.lastChatMessage,
            lastChatTime = domainModel.lastChatTime,
            un_read_chat_count = domainModel.unReadChatCount,
            participantList = userMapper.mapToEntityList(domainModel.participantList)
        )
    }

    fun mapFromEntityList(entityList: List<ChatRoomEntity>): List<ChatRoomData> {
        return entityList.map { chatRoomEntity ->
            mapFromEntity(chatRoomEntity)
        }
    }
}