package com.example.scannertest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.example.scannertest.DAL.OkHttpGet;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScansBrowserActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String path;
    private String storagePath;
    private List<String> dataSet;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scans_browser);

        dataSet = this.getDataFromServer();

        if(dataSet.size() == 1 && dataSet.get(0)==""){
            setContentView(R.layout.activity_scans_browser_empty);
        }else{
            recyclerView = findViewById(R.id.rvFileViewer);

            recyclerView.setHasFixedSize(true);

            layoutManager = new GridLayoutManager(this,3);
            recyclerView.setLayoutManager(layoutManager);

            mAdapter = new MyAdapter(dataSet);
            recyclerView.setAdapter(mAdapter);
        }
    }

    private List<String> getDataFromServer(){
        path = "";
        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
            Uri uri = Uri.parse(path);
            setTitle(uri.getLastPathSegment());
        }else{
            setTitle(AppSettings.APP_ROOT_FOLDER);
        }

        List<String> values = new ArrayList<>();
        String httpResult;
        preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");
        try {
            httpResult = OkHttpGet.okGetRequestWithPath(AppSettings.SERVER_ADDRESS + AppSettings.FILE_MANAGER_API, path, token);
            values = Arrays.asList(httpResult.split("\\s*,\\s*"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        //setTitle(getTitle() + AppSettings.CANT_READ_DIR);

        Collections.sort(values);
        return values;
    }

    private List<File> getDataFromLocalStorage(){
        storagePath = Environment.getExternalStorageDirectory().toString() + File.separator ;
        path = File.separator + AppSettings.APP_ROOT_FOLDER;
        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
            path = path.substring(Environment.getExternalStorageDirectory().toString().length());
        }
        setTitle(path.substring(1));

        List<String> values = new ArrayList<>();
        List<File> files = new ArrayList<>();
        File dir = new File(storagePath + path + File.separator);
        if(!dir.canRead()){
            setTitle(getTitle() + AppSettings.CANT_READ_DIR);
        }
        String[] list = dir.list();
        if(list != null){
            for(String file : list){
                if(!file.startsWith(".")){
                    values.add(file);
                    files.add(new File(storagePath + path + File.separator + file));
                }
            }
        }
        Collections.sort(files);
        return files;
    }

}