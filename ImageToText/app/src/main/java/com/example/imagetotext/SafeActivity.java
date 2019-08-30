package com.example.imagetotext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SafeActivity extends AppCompatActivity {

    private static final String TAG = "SafeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe);
        Log.d(TAG, "onCreate: ");
    }
}
