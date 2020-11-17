package com.example.taximagangue.provider;

import com.example.taximagangue.models.FCMBody;
import com.example.taximagangue.models.FCMResponse;
import com.example.taximagangue.retrofit.IFCMApi;
import com.example.taximagangue.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {
    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> setdNotifcation(FCMBody body){
        return RetrofitClient.getClienteObject(url).create(IFCMApi.class).send(body);
    }
}
