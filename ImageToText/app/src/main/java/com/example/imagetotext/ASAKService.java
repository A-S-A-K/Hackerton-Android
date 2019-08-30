package com.example.imagetotext;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ASAKService {

    String API_URL="";

    @FormUrlEncoded
    @POST("chemical_data.php")
    Call<ResponseBody> sendChemical(@Field("result") String result);

    @FormUrlEncoded
    @POST(" ")
    Call<ResponseBody> sendChemical2(@Field("result") String result);



}
