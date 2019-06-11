package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Util.Server;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText txtIDUser, txtNamaUser, txtPassword, txtKonfirmasiPassword;
    private TextInputLayout validasiIDUser, validasiNamaUser,
            validasiPassword, validasiKonfirmasiPassword;
    private Button btnRegister;
    private ProgressBar loading;
    static String response = "";

    private static String URL = "https://cellin-test.azurewebsites.net/koneksi/registrasi.php";

    private String id_user, nama_user, password, konfirmasi_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        txtIDUser = findViewById(R.id.txtIDUser);
        txtNamaUser = findViewById(R.id.txtNamaUser);
        txtPassword = findViewById(R.id.txtPassword);
        txtKonfirmasiPassword = findViewById(R.id.txtKonfirmasiPassword);

        validasiIDUser = findViewById(R.id.validasiIDUser);
        validasiNamaUser = findViewById(R.id.validasiNamaUser);
        validasiPassword = findViewById(R.id.validasiPassword);
        validasiKonfirmasiPassword = findViewById(R.id.validasiKonfirmasiPassword);

        btnRegister = findViewById(R.id.btnRegister);
        loading = findViewById(R.id.loading);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id_user = txtIDUser.getText().toString().trim();
                nama_user = txtNamaUser.getText().toString().trim();
                password = txtPassword.getText().toString().trim();
                konfirmasi_password = txtKonfirmasiPassword.getText().toString().trim();
                if(id_user.isEmpty()) {
                    validasiIDUser.setError("ID. User harus diisi!");
                }else if(nama_user.isEmpty()){
                    validasiNamaUser.setError("Nama User harus diisi!");
                }else if(password.isEmpty()){
                    validasiPassword.setError("Password harus diisi!");
                }else if(!konfirmasi_password.equals(password)){
                    validasiKonfirmasiPassword.setError("Konfirmasi password harus sama!");
                }else{
                    Registrasi();
                }

            }
        });
    }

    private void Registrasi() {
        loading.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.GONE);
        id_user = this.txtIDUser.getText().toString().trim();
        nama_user = this.txtNamaUser.getText().toString().trim();
        password = this.txtPassword.getText().toString().trim();
        konfirmasi_password = this.txtKonfirmasiPassword.getText().toString().trim();
        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            if(success.equals("1")){
                                Toast.makeText(Register.this, "Register Success!", Toast.LENGTH_SHORT).show();
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                            Toast.makeText(Register.this, "Register Error : " +e.toString(), Toast.LENGTH_SHORT).show();
                            loading.setVisibility(View.GONE);
                            btnRegister.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Register.this, "Register Error : " +error.toString(), Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
                btnRegister.setVisibility(View.VISIBLE);
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_user", id_user);
                params.put("nama_user", nama_user);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

}