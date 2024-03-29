package org.personal.videotogether.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.util.view.ViewHandler

class ChatRoomAdapter
constructor(
    val context: Context,
    private val isSelectable: Boolean,
    private val myUserId: Int,
    private var chatRoomList: ArrayList<ChatRoomData>,
    private val viewHandler: ViewHandler,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private val itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        val chatRoomProfileIV: ImageView = itemView.findViewById(R.id.chatRoomProfileIV)
        val nameTV: TextView = itemView.findViewById(R.id.nameTV)
        val participantCountTV: TextView = itemView.findViewById(R.id.participantsCountTV)
        val latestChatMessageTV: TextView = itemView.findViewById(R.id.latestChatMessageTV)
        val latestMessageTimeTV: TextView = itemView.findViewById(R.id.latestMessageTimeTV)
        val unReadMessageCountTV: TextView = itemView.findViewById(R.id.unReadMessageCountTV)
        val checkBoxCB: CheckBox = itemView.findViewById(R.id.checkboxCB)

        override fun onClick(view: View?) {

            if (adapterPosition != RecyclerView.NO_POSITION) {

                itemClickListener.onItemClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)

        if (isSelectable) view.findViewById<CheckBox>(R.id.checkboxCB).visibility = View.VISIBLE

        return ViewHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int {
        return chatRoomList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatRoomData = chatRoomList[position]
        val participantCount = chatRoomData.participantList.count() - 1

        // 내가 아닌 다른 사람을 채팅방 프로필 이미지로 설정
        // TODO : 시간이 남으면 여러 이미지 보여줄 수 있도록 구현하기
        for (participant in chatRoomData.participantList) {
            if (participant.id != myUserId) {
                Glide.with(context).load(participant.profileImageUrl).into(holder.chatRoomProfileIV)
                break
            }
        }

        holder.nameTV.text = viewHandler.formChatRoomName(chatRoomData.participantList, myUserId)
        holder.latestChatMessageTV.text = chatRoomData.lastChatMessage
        holder.participantCountTV.text = if (participantCount > 1) {
            participantCount.toString()
        } else {
            ""
        }

        // 채팅 방 선택하는 프래그먼트에 사용할 때
        if (isSelectable) {
            if (chatRoomData.isSelected != null) {
                holder.checkBoxCB.isChecked = chatRoomData.isSelected!!
            }
            // 채팅 방만 보여주고 싶을 때
        } else {
            // 마지막 채팅 메시지
            holder.latestMessageTimeTV.text = chatRoomData.lastChatTime
            // 안읽은 채팅 메시지 갯수 관련
            if (chatRoomData.unReadChatCount == 0) {
                holder.unReadMessageCountTV.visibility = View.GONE
            } else {
                holder.unReadMessageCountTV.visibility = View.VISIBLE
                holder.unReadMessageCountTV.text = chatRoomData.unReadChatCount.toString()
            }
        }
    }

    fun filterList(filteredChatRoomList: ArrayList<ChatRoomData>) {
        chatRoomList = filteredChatRoomList
        Log.i("TAG", "filterList: $chatRoomList")
        Log.i("TAG", "filterList: $filteredChatRoomList")
        notifyDataSetChanged()
    }
}