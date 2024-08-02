package io.agora.transcription_widget_demo.model

data class ServerConfig(
    var name: String,
    var serverUrl: String,
    var testIp: String,
    var testPort: Int,
    var appId:String,
    var appCert:String,
)
