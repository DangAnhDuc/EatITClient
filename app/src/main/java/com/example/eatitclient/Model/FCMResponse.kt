package com.example.eatitclient.Model

class FCMResponse {
    val multicast_id: Long? = 0
    var succes: Int = 0
    var failure: Int = 0
    var canonical_ids: Int = 0
    var result: List<FCMResult>? = null
    var message_id: Long = 0
}