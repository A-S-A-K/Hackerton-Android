package com.example.imagetotext;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UnSafeActivity extends AppCompatActivity {

    String chemicalData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_safe);

        Intent intent = getIntent();
        //텍스트 추출 데이터 넘어온 값..
        chemicalData = intent.getStringExtra("result");
    }
}
