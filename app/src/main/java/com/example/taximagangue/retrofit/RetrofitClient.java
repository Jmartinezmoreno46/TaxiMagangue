package com.example.taximagangue.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    public static Retrofit retrofic = null;

    public  static Retrofit getCliente(String url){
        if (retrofic ==null){
            retrofic = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return  retrofic;
    }


}
