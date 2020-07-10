package com.example.scannertest.DAL;

import android.content.SharedPreferences;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class OkHttpGet {
    OkHttpClient client = new OkHttpClient();
    private static String responseString;
    private static int responseStatus = 0;
    private SharedPreferences preferences;

    public static void main(String[] args) throws IOException {

    }

    public static String okGetRequestWithPath(String url, String path, String token) throws IOException, InterruptedException {
        OkHttpGet okHttpGet = new OkHttpGet();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("path", path);
        String finalUrl = urlBuilder.build().toString();
        Log.v("Axel","Print URL : " + finalUrl);
        responseStatus = 0;
        okHttpGet.run(finalUrl, token);
        while(responseStatus == 0){
            Thread.sleep(200);
        }
        Log.v("Axel","response: "+responseString);
        return responseString;
    }

    public static String okGetRequestWithParameter(String url, String parameterName, String parameterValue, String token) throws IOException, InterruptedException {
        OkHttpGet okHttpGet = new OkHttpGet();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter(parameterName, parameterValue);
        String finalUrl = urlBuilder.build().toString();
        Log.v("Axel","Print URL : " + finalUrl);
        responseStatus = 0;
        okHttpGet.run(finalUrl, token);
        while(responseStatus == 0){
            Thread.sleep(200);
        }
        Log.v("Axel","response: "+responseString);
        return responseString;
    }

    // Common method to execute and return response
    public void run(String url, String token) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", token)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.print("Failure to get connection");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                responseString = response.body().string();
                Log.v("Axel", "okhhtpget response statuds code: "+response.code());
                responseStatus = 1;
            }
        });

    }
}
