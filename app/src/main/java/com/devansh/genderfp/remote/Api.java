package com.devansh.genderfp.remote;



import com.devansh.genderfp.model.ImageInfo;

import okhttp3.MultipartBody;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {

    @GET("./")
    Call<String> getResult();

    @Multipart
    @POST("./")
    Call<ImageInfo> uploadImage(@Part MultipartBody.Part file);
}
