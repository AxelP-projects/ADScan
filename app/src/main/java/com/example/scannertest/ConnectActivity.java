package com.example.scannertest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectActivity extends AppCompatActivity {
    private EditText etId;
    private EditText etPw;
    private Button btnSubmit;
    private CheckBox cbRememberMe;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPreferencesEditor;
    private Boolean saveLogin;
    private String token;
    private int responseStatusCode;
    private final int HTTP_SUCCESS = 200;
    private final int COULD_NOT_CONNECT = -1;
    private String responseToken;
    private boolean requestFinished;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        etId = findViewById(R.id.etIdConnect);
        etPw = findViewById(R.id.etPwConnect);
        btnSubmit = findViewById(R.id.btnSubmitConnect);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPreferencesEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            etId.setText(loginPreferences.getString("username", ""));
            etPw.setText(loginPreferences.getString("password", ""));
            cbRememberMe.setChecked(true);
        }

    }


    public void connect(View view) {
        if(!TextUtils.isEmpty(etId.getText().toString().trim()) && !TextUtils.isEmpty(etPw.getText().toString().trim())){
            String id = etId.getText().toString().trim();
            String pw = etPw.getText().toString().trim();

            Log.v("Axel", id + " "+ pw);
            validateUser(id,pw);
            while (requestFinished == false){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(responseStatusCode != COULD_NOT_CONNECT && !responseToken.equals("")){
                if(responseStatusCode == HTTP_SUCCESS){
                    if (cbRememberMe.isChecked()) {
                        loginPreferencesEditor.putBoolean("saveLogin", true);
                        loginPreferencesEditor.putString("username", id);
                        loginPreferencesEditor.putString("password", pw);
                        loginPreferencesEditor.commit();
                    } else {
                        loginPreferencesEditor.clear();
                        loginPreferencesEditor.commit();
                    }
                    loginPreferencesEditor.putString("token", responseToken);
                    loginPreferencesEditor.commit();

                    Intent intent = new Intent();
                    this.setResult(Activity.RESULT_OK,intent);
                    this.finish();
                }else{
                    Toast.makeText(this, "L'identifiant et/ou le mot de passe sont erronés",
                            Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this, "Impossible de se connecter au serveur",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean validateUser(String id, String pw) {
        responseStatusCode = 0;
        responseToken = "";
        requestFinished = false;
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", id)
                .addFormDataPart("password", pw)
                .build();

        Request request = new Request.Builder()
                .url(AppSettings.SERVER_ADDRESS + AppSettings.CONNECT_API)
                .post(formBody)
                .build();
        Log.v("Axel", AppSettings.SERVER_ADDRESS + AppSettings.CONNECT_API);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onFailureConstructor();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                onResponseConstructor(response.body().string(), response.code());
            }
        });
        return true;
    }

    private void onResponseConstructor(String token, int code) throws IOException {
        responseStatusCode = code;
        responseToken = token;
        requestFinished = true;
    }

    private void onFailureConstructor() {
        responseStatusCode = COULD_NOT_CONNECT;
        requestFinished = true;
    }
}