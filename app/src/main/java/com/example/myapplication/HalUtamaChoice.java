package com.example.myapplication;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;

public class HalUtamaChoice extends AppCompatActivity {
    Button btn2G, btn3G, btn4G;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hal_choice);
        btn2G = (Button) findViewById(R.id.btn2G);
        btn3G = (Button) findViewById(R.id.btn3G);
        btn4G = (Button) findViewById(R.id.btn4G);
        imageView = (ImageView) findViewById(R.id.cellIn);

        Glide.with(getApplicationContext()).load(R.drawable.cellin).into(imageView);

        btn2G.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2G = new Intent(getApplicationContext(),HalUtama2G.class);
                startActivity(intent2G);
            }
        });
        btn3G.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3G = new Intent(getApplicationContext(),HalUtama3G.class);
                startActivity(intent3G);
            }
        });
        btn4G.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent4G = new Intent(getApplicationContext(),HalUtama4G.class);
                startActivity(intent4G);
            }
        });
    }
}
