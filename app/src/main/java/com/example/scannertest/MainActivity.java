package com.example.scannertest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.scannertest.bo.ScannedPhoto;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int CAMERA_REQUEST_CODE = 1234;
    private final int STORAGE_REQUEST_CODE = 1235;
    private final int SCANNER_ACTIVITY_REQUEST_CODE = 99;
    private final int PHOTO_DIRECTIVES_ACTIVITY_REQUEST_CODE = 101;
    private final int CONNECT_ACTIVITY_REQUEST_CODE = 102;

    private ScannedPhoto scannedPhoto;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, ConnectActivity.class);
        startActivityForResult(intent, CONNECT_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_REQUEST_CODE){
            if(grantResults.length>0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                scannedPhoto = new ScannedPhoto();
                int preference = ScanConstants.OPEN_CAMERA;
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
                startActivityForResult(intent, SCANNER_ACTIVITY_REQUEST_CODE);
            }        else {
                Toast.makeText(MainActivity.this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        if(requestCode == STORAGE_REQUEST_CODE){
            if(grantResults.length>0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                int preference = ScanConstants.OPEN_MEDIA;
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
                startActivityForResult(intent, SCANNER_ACTIVITY_REQUEST_CODE);
            }        else {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void openCamera(View v){
        ActivityCompat.requestPermissions(this, permissions,CAMERA_REQUEST_CODE);
    }

    //Deleted implementation, just link a button on activity_main to this to activate. Is broken tho.
    public void openGallery(View v){
        ActivityCompat.requestPermissions(this, permissions,STORAGE_REQUEST_CODE);
    }

    public void openScans(View v){
        Intent intent = new Intent(this, ScansBrowserActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONNECT_ACTIVITY_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                setContentView(R.layout.activity_main);
            }else{
                Intent intent = new Intent(this, ConnectActivity.class);
                startActivityForResult(intent, CONNECT_ACTIVITY_REQUEST_CODE);
            }
        }
        if (requestCode == SCANNER_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(scannedPhoto == null){
                scannedPhoto = new ScannedPhoto();

            }
            scannedPhoto.setUri((Uri) data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT));

            if(data.getExtras().getBoolean("Continue")){
                int preference = ScanConstants.OPEN_CAMERA;
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
                startActivityForResult(intent, SCANNER_ACTIVITY_REQUEST_CODE);
            }else{
                Log.v("Axel",scannedPhoto.toString());
                Intent intent = new Intent(this, PhotoDirectivesActivity.class);
                intent.putExtra("scannedPhoto", scannedPhoto);
                startActivityForResult(intent, PHOTO_DIRECTIVES_ACTIVITY_REQUEST_CODE);
            }
        }
        if (requestCode == PHOTO_DIRECTIVES_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            scannedPhoto = data.getParcelableExtra("scannedPhoto");
            savePhotoAsPDF(data);
        }
    }


    protected void savePhotoAsPDF(@Nullable Intent data) {
        Bitmap bitmap = null;
        PdfDocument.PageInfo pageInfo = null;
        try {
            PdfDocument document = new PdfDocument();
            for(int i=0; i<scannedPhoto.getUri().size(); i++){
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), scannedPhoto.getUri().get(i));
                getContentResolver().delete(scannedPhoto.getUri().get(i), null, null);
                pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), i+1).create();

                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#ffffff"));
                canvas.drawPaint(paint);

                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                paint.setColor(Color.BLUE);
                canvas.drawBitmap(bitmap, 0, 0 , null);
                document.finishPage(page);
            }

            if(checkIfFolderExists(AppSettings.APP_ROOT_FOLDER) && checkIfFolderExists(AppSettings.APP_ROOT_FOLDER + File.separator  + scannedPhoto.getFolder())) {
                final String filePath = Environment.getExternalStorageDirectory().toString() + File.separator + AppSettings.APP_ROOT_FOLDER + File.separator + scannedPhoto.getFolder() + File.separator;
                final String fileName = scannedPhoto.getName();
                Log.v("Axel", "lenomduficher  :  "+fileName+"le path : " + filePath+" destfolder : "+ scannedPhoto.getFolder());
                File file = new File(filePath, fileName);
                document.writeTo(new FileOutputStream(file));

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //messageText.setText("uploading started.....");
                            }
                        });
                        uploadFileUsingOkHttp(filePath + fileName);

                    }
                }).start();
            }
            document.close();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, "L'écriture du pdf a échoué " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean uploadFileUsingOkHttp(String sourceUri){
        OkHttpClient client = new OkHttpClient();
        File sourceFile = new File(sourceUri);
        String result;
        
        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File not exist :"
                    + scannedPhoto.getFolder() + "" + scannedPhoto.getName());
        }else{
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", scannedPhoto.getName(),
                            RequestBody.create(MediaType.parse("multipart/form-data"), sourceFile))
                    .addFormDataPart("destinationFolder", scannedPhoto.getFolder())
                    .build();
            Log.v("Axel","folder de dest : "+scannedPhoto.getFolder());
            preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            String token = preferences.getString("token", "");
            Request request = new Request.Builder()
                    .url(AppSettings.SERVER_ADDRESS + AppSettings.FILE_MANAGER_API)
                    .post(requestBody)
                    .header("Authorization", token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    onUploadFileFail();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    onUploadFileSuccess(response.code(), response.body().string());
                }
            });
        }
        return true;
    }

    private void onUploadFileFail(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,
                        "Echec de connexion au serveur", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onUploadFileSuccess(int code, String body){
        if(code == 200){
            if(body.equals("UPLOAD_FAILED")){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Echec de la création du fichier sur le serveur.", Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Upload terminé!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }else if(code == 403){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "Erreur d'authentification, veuillez redémarrer l'appli et vous reconnecter.", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "Erreur.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    protected boolean checkIfFolderExists(String folderName){
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator+folderName);
        boolean success = true;
        try{
            if(!(dir.exists()) && !(dir.isDirectory())) {
                success = dir.mkdirs();
            }
            if(success==true){
                return success;
            }else{
                throw new Exception("Impossible de créer le dossier de destination");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
