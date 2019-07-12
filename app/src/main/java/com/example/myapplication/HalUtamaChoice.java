package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

public class HalUtamaChoice extends AppCompatActivity {
    Button btn2G, btn3G, btn4G, btnLogout;
    ImageView imageView;
    TextView usernameTextView;
    String username;
    String fullname;
    SessionHandler sessionHandler;
    SharedPreferences mSharedPreference;
    User user;
    public static final String TAG = "HalUtamaChoice";

    /**
     * Id to identify a camera permission request.
     */
    private static final int REQUEST_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hal_choice);
        sessionHandler = new SessionHandler(getApplicationContext());
        btn2G = (Button) findViewById(R.id.btn2G);
        btn3G = (Button) findViewById(R.id.btn3G);
        btn4G = (Button) findViewById(R.id.btn4G);
        btnLogout = (Button) findViewById(R.id.logoutId);
        usernameTextView = (TextView)findViewById(R.id.username);
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        username = prefs.getString("username", "UNKNOWN");
        username = sessionHandler.setKeyUsername();
        usernameTextView.setText(username);
//        Glide.with(getApplicationContext()).load(R.drawable.cellin).into(imageView);

        btn2G.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs2g = getSharedPreferences("HalUtama2G", MODE_PRIVATE);
                prefs2g.edit().putString("username", username).apply();
                Intent intent2G = new Intent(getApplicationContext(), HalUtama2G.class);
                intent2G.putExtra("usernameIntent",username);
                startActivity(intent2G);
            }
        });
        btn3G.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs3g = getSharedPreferences("HalUtama3G", MODE_PRIVATE);
                prefs3g.edit().putString("username", username).apply();
                Intent intent3G = new Intent(getApplicationContext(), HalUtama3G.class);
                intent3G.putExtra("usernameIntent",username);
                startActivity(intent3G);
            }
        });
        btn4G.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs4g = getSharedPreferences("HalUtama4G", MODE_PRIVATE);
                prefs4g.edit().putString("username", username).apply();
                Intent intent4G = new Intent(getApplicationContext(), HalUtama4G.class);
                intent4G.putExtra("usernameIntent",username);
                startActivity(intent4G);
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionHandler.logoutUser();
                Intent intent = new Intent(getApplicationContext(),LogIn.class);
                startActivity(intent);
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.

            requestLocationPermission();

        } else {

            // Camera permissions is already available, show the camera preview.
            Log.i(TAG,
                    "LOCATION permission has already been granted.");
//            showCameraPreview();
        }
        // END_INCLUDE(camera_permission)

    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestLocationPermission() {
        Log.i(TAG, "LOCATION permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying location permission rationale to provide additional context.");
            Snackbar.make(btn4G, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(HalUtamaChoice.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
        // END_INCLUDE(camera_permission_request)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Location permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "LOCATION permission has now been granted. Showing preview.");
                Snackbar.make(btn4G, R.string.permision_available_location,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "Location permission was NOT granted.");
                Snackbar.make(btn4G, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)


        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}
