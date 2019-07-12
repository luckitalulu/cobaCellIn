package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Upload extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final String TAG = Upload.class.getSimpleName();
    private String selectedFilePath;
    private String SERVER_URL_2G = "http://cellin.tech/uploadtoserver2g.php";
    private String SERVER_URL_3G = "http://cellin.tech/uploadtoserver3g.php";
    private String SERVER_URL_4G = "http://cellin.tech/uploadtoserver4g.php";
    String SERVER_URL;
    ImageView varIvAttachment;
    Button bUpload, chooseFile;
    TextView tvFileName;
    ProgressDialog dialog;
    Spinner spinner;
    Context uploadContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        spinner = (Spinner)findViewById(R.id.sp_name);
        if (spinner.getSelectedItem().equals("2G")){
            SERVER_URL = SERVER_URL_2G;
        }
        else if (spinner.getSelectedItem().equals("3G")){
            SERVER_URL = SERVER_URL_3G;
        }
        else if (spinner.getSelectedItem().equals("4G")){
            SERVER_URL = SERVER_URL_4G;
        }
        bUpload = (Button) findViewById(R.id.upload_file);
        chooseFile =(Button) findViewById(R.id.upload);
        tvFileName = (TextView)findViewById(R.id.selected_file_name);
        varIvAttachment = (ImageView)findViewById(R.id.ivAttachment);



        Glide.with(getApplicationContext()).load(R.drawable.cellin).into(varIvAttachment);
        bUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFilePath != null){
                    dialog = ProgressDialog.show(Upload.this,"","Uploading File...",true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //creating new thread to handle Http Operations
                            uploadFile();
                        }
                    }).start();
                }else{
                    Toast.makeText(Upload.this,"Please choose a File First",Toast.LENGTH_SHORT).show();
                }

            }
        });

        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
    }


    private void showFileChooser() {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("*/*");

        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_FILE_REQUEST){
                if(data == null){
                    //no data present
                    return;
                }


                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this,selectedFileUri);
                Log.i(TAG,"Selected File Path:" + selectedFilePath);

                if(selectedFilePath != null && !selectedFilePath.equals("")){
                    tvFileName.setText(selectedFilePath);
                }else{
                    Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //android upload file to server

    private void uploadFile() {
        // Map is used to multipart the file using okhttp3.RequestBody
        File file = new File(selectedFilePath);

        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());

        if (spinner.getSelectedItem().equals("4G")){
            ApiConfig4g getResponse = AppConfig.getRetrofit().create(ApiConfig4g.class);
            Call<ServerResponse> call = getResponse.uploadFile(fileToUpload, filename);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        if (serverResponse.getSuccess()) {
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        assert serverResponse != null;
//                        Log.v("Response", serverResponse.toString());
                    }
                    dialog.dismiss();
                }
                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {

                }
            });
        } else if (spinner.getSelectedItem().equals("3G")){
            ApiConfig3g getResponse = AppConfig.getRetrofit().create(ApiConfig3g.class);
            Call<ServerResponse> call = getResponse.uploadFile(fileToUpload, filename);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        if (serverResponse.getSuccess()) {
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        assert serverResponse != null;
                        Log.v("Response", serverResponse.toString());
                    }
                    dialog.dismiss();
                }
                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {

                }
            });
        } else if (spinner.getSelectedItem().equals("2G")){
            ApiConfig2g getResponse = AppConfig.getRetrofit().create(ApiConfig2g.class);
            Call<ServerResponse> call = getResponse.uploadFile(fileToUpload, filename);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        if (serverResponse.getSuccess()) {
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        assert serverResponse != null;
                        Log.v("Response", serverResponse.toString());
                    }
                    dialog.dismiss();
                }
                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {

                }
            });
        }




    }

}