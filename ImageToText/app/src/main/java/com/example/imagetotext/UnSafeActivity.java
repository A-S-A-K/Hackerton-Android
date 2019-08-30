package com.example.imagetotext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UnSafeActivity extends AppCompatActivity {

    String chemicalData;
    Retrofit retrofit;
    ASAKService asakService;

    ArrayList<String> productList_text;
    ArrayList<Integer> productList_num;
    ImageView iv_1, iv_2, iv_3, iv_4, iv_5;
    TextView tv_1, tv_2, tv_3, tv_4, tv_5, tv_danger;
    JSONArray marray;

    private static final String TAG = "UnSafeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_safe);

        Intent intent = getIntent();
        //텍스트 추출 데이터 넘어온 값..
        chemicalData = intent.getStringExtra("result");

        iv_1 = findViewById(R.id.unsafe_iv_1);
        iv_2 = findViewById(R.id.unsafe_iv_2);
        iv_3 = findViewById(R.id.unsafe_iv_3);
        iv_4 = findViewById(R.id.unsafe_iv_4);
        iv_5 = findViewById(R.id.unsafe_iv_5);

        tv_1 = findViewById(R.id.unsafe_tv_1);
        tv_2 = findViewById(R.id.unsafe_tv_2);
        tv_3 = findViewById(R.id.unsafe_tv_3);
        tv_4 = findViewById(R.id.unsafe_tv_4);
        tv_5 = findViewById(R.id.unsafe_tv_5);

        tv_danger = findViewById(R.id.tv_danger_chemic);

        RetrofitData1();
    }

    private void RetrofitData1(){
        //원용이 서버로 보내는 코드
        retrofit = new Retrofit.Builder().baseUrl(ASAKService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        asakService = retrofit.create(ASAKService.class);

        Call<ResponseBody> sendChemical = asakService.sendChemical(chemicalData);
        sendChemical.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String returnData = response.body().string();
                    marray =  new JSONArray(returnData);
                    tv_danger.setText(marray.get(0).toString()+"," + marray.get(1).toString()+"," + marray.get(2).toString());

                    Log.d("UnsafeAc return 값-1", returnData);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("UnsafeAc return 값-1", "OnResponse: "+ t.getMessage());

            }
        });

        //지수씨 서버로 보내는 코드.. 인공지능 5개 추출
        retrofit = new Retrofit.Builder().baseUrl("http://192.168.1.8")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        asakService = retrofit.create(ASAKService.class);

//        Log.d(TAG, "RetrofitData1: "+chemicalData);
        Call<ResponseBody> sendChemical2 = asakService.sendChemical2(chemicalData);
        sendChemical2.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if(response.isSuccessful()){
                        String returnData2 = response.body().string();
                        Log.d("UnsafeAc return 값-2", returnData2);


                    }else{
                        Log.d(TAG, "onResponse 2: "+ chemicalData);
                        String url = String.valueOf(response.raw().request().url());
                        //요청 url 로그로 받아오는 코드
                        Log.d(TAG, "onResponse url: "+ url);
                    }
                    randomMatchingItem();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("UnsafeAc return 값-2", "OnResponse: "+ t.getMessage());

            }
        });
    }

    private void randomMatchingItem() {
        //인공지능에서 받아오는 데이터에 랜덤으로 제품 매칭
        //제품 이름
        productList_text = new ArrayList<>();
        productList_text.add("고체형 약산성 파우더");
        productList_text.add("그린 데빌 파워 본드");
        productList_text.add("노바 티어 아이드롭스");
        productList_text.add("닥터 브로너스 바디 클리너");
        productList_text.add("바이오 언더암 데오드란트 크림");
        productList_text.add("배쓰 바디 포켓 퍼퓨머");
        productList_text.add("벨벨 네일아트");
        productList_text.add("봄버블 나프탈렌 방향제");
        productList_text.add("브로맥스 시니컬 아이드롭 안약");
        productList_text.add("블랙캣 라이트 칼라 립스틱");
        productList_text.add("석탄 비누");
        productList_text.add("수퍼 하드 캡사이신");
        productList_text.add("쉴드 가드 흡수 패드");
        productList_text.add("스타더스트 색연필");
        productList_text.add("시트라 아우라 세제 액체형");
        productList_text.add("썸머 바디 로션");
        productList_text.add("아카이브 스네일 화이트 크림");
        productList_text.add("아코넴 데일리 케어 화장솜");
        productList_text.add("아토마이저 페로몬 향수");
        productList_text.add("오가닉 다용도 오일");
        productList_text.add("인공 섬유 클린징 스펀지");
        productList_text.add("카토마 컬러 지점토");
        productList_text.add("케빈스 코티지 허브 오일");
        productList_text.add("코르커스 그린치 샤워젤");
        productList_text.add("투씨 바이럴 락스");
        productList_text.add("페로페로 샴푸");
        productList_text.add("페트리샤 인공 장미 세트");
        productList_text.add("프로펙스 맨즈 단백질 보충제");
        productList_text.add("한진 제약 대나무 면봉");
        productList_text.add("허클베리 프레스티지 퍼퓨머");

        //제품 이미지
        productList_num = new ArrayList<>();


        //랜덤 함수로 숫자 5개 추출
        int num;
        Random random = new Random(); //객체 생성

        for(int i=0; i < 5; i++){
            num = random.nextInt(30)+1;
            productList_num.add(num);

            if(i == 0){
                Glide.with(this).load(Uri.parse("http://13.124.22.195/hackerton/"+productList_num.get(i)+".jpg"))
                        .into(iv_1);
                tv_1.setText(productList_text.get(productList_num.get(i)-1));
                //text 고정
            }else if(i==1){
                Glide.with(this).load(Uri.parse("http://13.124.22.195/hackerton/"+productList_num.get(i)+".jpg"))
                        .into(iv_2);
                tv_2.setText(productList_text.get(productList_num.get(i)-1));

            }else if(i==2){
                Glide.with(this).load(Uri.parse("http://13.124.22.195/hackerton/"+productList_num.get(i)+".jpg"))
                        .into(iv_3);
                tv_3.setText(productList_text.get(productList_num.get(i)-1));

            }else if(i==3){
                Glide.with(this).load(Uri.parse("http://13.124.22.195/hackerton/"+productList_num.get(i)+".jpg"))
                        .into(iv_4);
                tv_4.setText(productList_text.get(productList_num.get(i)-1));

            }else {
                Glide.with(this).load(Uri.parse("http://13.124.22.195/hackerton/"+productList_num.get(i)+".jpg"))
                        .into(iv_5);
                tv_5.setText(productList_text.get(productList_num.get(i)-1));

            }

            Log.d("random num", String.valueOf(num));
        }



    }
}
