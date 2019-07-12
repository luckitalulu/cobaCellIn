package com.example.myapplication;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
interface ApiConfig4g {
    @Multipart
    @POST ("uploadtoserver4g.php")
    Call<ServerResponse> uploadFile(@Part MultipartBody.Part file,
                                    @Part("file") RequestBody name);
}
interface ApiConfig3g {
    @Multipart
    @POST("uploadtoserver3g.php")
    Call<ServerResponse> uploadFile(@Part MultipartBody.Part file,
                                    @Part("file") RequestBody name);
}
interface ApiConfig2g {
    @Multipart
    @POST("uploadtoserver2g.php")
    Call<ServerResponse> uploadFile(@Part MultipartBody.Part file,
                                    @Part("file") RequestBody name);
}
