package com.example.scannertest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.scannertest.DAL.OkHttpGet;
import com.example.scannertest.bo.ScannedPhoto;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PhotoDirectivesActivity extends AppCompatActivity {

    private ScannedPhoto scannedPhoto;
    private EditText etFileName;
    private Spinner spinnerFolder;
    private CheckBox cbDate;
    private CheckBox cbFolder;
    ArrayAdapter<String> adapter;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_directives);
        this.etFileName = findViewById(R.id.editTextFileName);
        this.spinnerFolder = findViewById(R.id.spinFolder);
        this.cbDate = findViewById(R.id.cbDate);
        this.cbFolder = findViewById(R.id.cbFolder);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getSpinnerValues());
        spinnerFolder.setAdapter(adapter);

        etFileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>0){
                    cbDate.setEnabled(true);
                    cbFolder.setEnabled(true);
                }else{
                    cbDate.setEnabled(false);
                    cbFolder.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private String [] getSpinnerValues(){
        String[] result = new String[5];
        String httpResult;
        preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");
        try {
            httpResult = OkHttpGet.okGetRequestWithParameter(AppSettings.SERVER_ADDRESS + AppSettings.FILE_MANAGER_API, "writable", "true", token);
            result = httpResult.split("\\s*,\\s*");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.sort(result);
        return result;
    }

    public void onValidate(View view){
        scannedPhoto = getIntent().getParcelableExtra("scannedPhoto");
        getPhotoInfos(view);
        Intent intent = new Intent();
        intent.putExtra("scannedPhoto", scannedPhoto);
        this.setResult(Activity.RESULT_OK,intent);
        this.finish();
    }

    public void getPhotoInfos(View view){
        String folderName;
        StringBuilder fileNameSB;
        if(!TextUtils.isEmpty(spinnerFolder.getSelectedItem().toString().trim())){
            folderName = spinnerFolder.getSelectedItem().toString().trim();
            scannedPhoto.setFolder(folderName);
        }else{
            folderName = AppSettings.APP_DEFAULT_FOLDER_NAME;
            scannedPhoto.setFolder(AppSettings.APP_DEFAULT_FOLDER);
        }
        if(!TextUtils.isEmpty(etFileName.getText().toString().trim())){
            fileNameSB = new StringBuilder();
            fileNameSB.append(etFileName.getText().toString().trim());
            if(cbDate.isChecked()){
                fileNameSB.append(AppSettings.DEFAULT_SPACE_CHAR + new SimpleDateFormat(AppSettings.DATE_FORMAT).format(new Date()));
            }
            if(cbFolder.isChecked()){
                fileNameSB.append(AppSettings.DEFAULT_SPACE_CHAR + folderName);
            }
            scannedPhoto.setName(fileNameSB.toString() + AppSettings.APP_DEFAULT_PHOTO_EXTENSION);
        }else{
            scannedPhoto.setName(new SimpleDateFormat(AppSettings.DATE_FORMAT).format(new Date()) + AppSettings.DEFAULT_SPACE_CHAR + folderName + AppSettings.APP_DEFAULT_PHOTO_EXTENSION);
        }
    }


}