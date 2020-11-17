package com.example.taximagangue.retrofit;

import com.example.taximagangue.models.FCMBody;
import com.example.taximagangue.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-type:application/json",
            "Authorization:key=AAAACwd3NbA:APA91bFGMAcyyyVWxOt1sJMzTFQ8bQ9XIpVs7Isi4HKcV_BXjdRMFUOe7MguPBrlGDubU17KcfkFiF3gWokR9v_YmlTMH5d2GMHHewxZWSA0u6Xd7GUwe_7c1YhzjIEeNpoQGQkAOKJk"
    })

      @POST("fcm/send")
      Call<FCMResponse> send(@Body FCMBody body);
}
