package com.example.taximagangue.provider;

import android.content.Context;

import com.example.taximagangue.R;
import com.example.taximagangue.retrofit.RetrofitClient;
import com.example.taximagangue.retrofit.iGoogleApi;
import com.google.android.gms.maps.model.LatLng;

import retrofit2.Call;


public class GoogleApiProvider {
    private Context context;


    public  GoogleApiProvider(Context context){
        this.context = context;
    }

    public Call<String> getDirections(LatLng originLatLng , LatLng destinationLatLng){
        String baseurl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + originLatLng.latitude + "," + originLatLng.longitude + "&"
                + "destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&"
                + "key=" + context.getResources().getString(R.string.google_maps_key);

        return RetrofitClient.getCliente(baseurl).create(iGoogleApi.class).getDirections(baseurl + query);
    }

}
