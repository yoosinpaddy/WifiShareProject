package com.passowrd.key.wifishare.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;

public interface ApiService {
    /*Endpoint call*/
    @POST("endpoint")
    Call<ResponseBody> requestMethod();
}
