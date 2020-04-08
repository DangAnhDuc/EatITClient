package com.example.eatitclient.Remote

import com.example.eatitclient.Model.FCMResponse
import com.example.eatitclient.Model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMServices {
    @Headers(
        "Content_Type: application/json",
        "Authorization: key=AAAAtWSoK-0:APA91bHKoiVhoULawikHuq3dda3zpZzaC3eR2eXyUJon_pOq2cCLeur7u6NvDYhDj6jEJSo8fT6MOCK28G0w68XN8E35e3wBWtH4UURBHgxWX3pQ7eie4fO6BOPWxYq2CP_ctXfuZ8Hn"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData): Observable<FCMResponse>
}