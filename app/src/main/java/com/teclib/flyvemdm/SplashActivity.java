package com.teclib.flyvemdm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.teclib.data.DataStorage;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        DataStorage cache = new DataStorage( SplashActivity.this );

        String broker = cache.getVariablePermanente("broker");
        if(broker != null) {

            abrirMain();

        }
    }

    private void abrirMain() {
        Intent miIntent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(miIntent);
        SplashActivity.this.finish();
    }
}
