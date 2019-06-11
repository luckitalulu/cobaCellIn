package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Util.Server;

import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import java.util.Map;


public class LogIn extends AppCompatActivity {

    // deklarasi objek
    TextInputLayout validasiIDUser, validasiPassword;
    EditText txtIDUser, txtPassword;
    Button btnLogin;
    TextView txtRegistrasi;

    // deklarasi variabel
    String id_user, password;

    // deklarasi variabel alamat host
    public static String URL = "https://cellin-test.azurewebsites.net/koneksi/login.php";

    com.example.myapplication.SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        sessionManager = new SessionManager(this);

        // inisialisasi variabel objek
        validasiIDUser = findViewById(R.id.validasiIDUser);
        validasiPassword = findViewById(R.id.validasiPassword);
        txtIDUser = findViewById(R.id.txtIDUser);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegistrasi = findViewById(R.id.txtRegister);

        // jika tombol login diklik
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id_user = txtIDUser.getText().toString().trim();
                password = txtPassword.getText().toString().trim();

                if(id_user.isEmpty()){
                    validasiIDUser.setError("ID. User harus diisi!");
                }else if(password.isEmpty()){
                    validasiIDUser.setError("Password harus diisi!");
                }else{
                    auth_user(id_user, password);
                }
            }
        });

        // jika tombol register diklik
        txtRegistrasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sintak untuk pindah activity
                Intent intent = new Intent(LogIn.this, Register.class);
                LogIn.this.startActivity(intent);
            }
        });

    }

    // method login
    private void auth_user(final String id_user, final String password){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("login");
                            if(success.equals("1")){
                                for(int i=0 ; i<jsonArray.length() ; i++){
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                    String nama_user = jsonObject1.getString("nama_user").trim();

                                    sessionManager.createSession(id_user, nama_user);

                                    Toast.makeText(LogIn.this,
                                            "Login berhasil ! \n Nama : "+nama_user,
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }else{
                                Toast.makeText(LogIn.this,
                                        "ID. User dan Password tidak ditemukan! ",
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LogIn.this,
                                    "Error login : " + e.toString(),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LogIn.this,
                                "Error login : " + error.toString(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_user", id_user);     // sesuaikan dengan $_POST pada PHP
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}