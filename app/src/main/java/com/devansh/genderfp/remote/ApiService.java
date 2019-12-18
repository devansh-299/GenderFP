package com.devansh.genderfp.remote;


import android.graphics.Bitmap;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private static final String BASE_URL = "http://f9122abb.ngrok.io/upload/";
    // the base url on top of which every thing is added

    private  Api api;

    public ApiService() {
        api = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(Api.class);
    }

    public   Api getClient () {
        return api ;
    }

}