package com.example.eatitclient.Remote

import com.example.eatitclient.Model.BraintreeToken
import com.example.eatitclient.Model.BraintreeTransaction
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ICloudFunctions {

    @GET("token")
    fun getToken(): Observable<BraintreeToken>

    @POST("checkout")
    @FormUrlEncoded
    fun submitPayment(
        @Field("amount") amount: Double,
        @Field("payment_method_nonce") nounce: String
    ): Observable<BraintreeTransaction>
}