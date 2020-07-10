package com.example.scannertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.scannertest.pdfDownload.Downloading;
import com.example.scannertest.pdfDownload.DownloadingImplementation;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class pdfView extends AppCompatActivity {
    private PDFView pdfView ;
    private String url ;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        pdfView = this.findViewById(R.id.pdfView);
        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
            setTitle(path.substring(1));
            HttpUrl.Builder urlBuilder = HttpUrl.parse(AppSettings.SERVER_ADDRESS + AppSettings.FILE_MANAGER_API).newBuilder();
            urlBuilder.addQueryParameter("path", path);
            url = urlBuilder.build().toString();
            download(url);
        }else{
            Log.v("Axel", "Missing extra");
        }
    }

    private void download(String url){
        String pdfFileName = url.substring(url.lastIndexOf('/') + 1);

        new DownloadingImplementation(this, new Handler(), new Downloading.Listener() {
            @Override
            public void onSuccess(String url, File pdfFile) {
                displayPdpFromFile(pdfFile);
            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onProgressUpdate(int progress, int total) {

            }
        }).download(url);

    }

    private void displayPdpFromFile(File file){
        pdfView.fromFile(file)
                .defaultPage(0)
                //.onPageChange(this)
                .enableAnnotationRendering(true)
                //.onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                //.onPageError(this)
                .load();
    }

    protected String getPdfFromServer(){//String path){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        String result = null;
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                try{

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(AppSettings.SERVER_ADDRESS + AppSettings.FILE_MANAGER_API)
                            //.addHeader("X-CSRFToken", csrftoken)

                            .addHeader("Content-Type", "application/json")
                            .build();
                    Response response = client.newCall(request).execute();

                    InputStream in = response.body().byteStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String result, line = reader.readLine();
                    result = line;
                    while((line = reader.readLine()) != null) {
                        result += line;
                    }
                    Log.v("Axel",response.body().toString());
                    response.body().close();
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        Future<String> future = executorService.submit(callable);
        try {
            result = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        Log.v("Axel", "Juste avant retour");

        return result;
    }


}