package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.UserData

sealed class SocketStateEvent {
    // tcp 관련
    object ConnectToTCPServer : SocketStateEvent()
    object DisconnectFromTCPServer: SocketStateEvent()
    data class RegisterSocket(val userData: UserData) : SocketStateEvent()
    data class SendToTCPServer(val flag: String, val firstMessage: String? = null, val secondMessage: String? = null) : SocketStateEvent()
    object ReceiveFromTCPServer : SocketStateEvent()
}