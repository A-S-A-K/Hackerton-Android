package com.example.imagetotext;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ASAKService {

    String API_URL="http://13.124.22.195/hackerton/";

    @FormUrlEncoded
    @POST("chemical_data.php")
    Call<ResponseBody> sendChemical(@Field("result") String result);


    @GET(":5000")
    Call<ResponseBody> sendChemical2(@Query("result") String result);



}
