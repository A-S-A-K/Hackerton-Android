package com.example.imagetotext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageContext;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText mResultEt;
    ImageView mPreviewIv;
    Button kobt, sendbt;
    String resulttext;
    /*
    해야할 것.
    1.인식된 한글을 구분자로 나눈다.
    2.나눈 한글을 리스트에 담는다.
    3.json성분파일을 리스트에 담는다.
    4.인식된 성분 리스트와 json 성분을 비교한다.
    */
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String storagePermission[];

    ArrayList<String> chemicalist, imagetotextlist, comparedlist;

    Uri image_uri;

    ProgressDialog progressDialog;

    private static final int KOREAN_TEXT =11;
    private static final int ERROR =12;
    Bitmap image; //사용되는 이미지
    String datapath = "" ; //언어데이터가 있는 경로

    Image inputImage;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case KOREAN_TEXT :
                    mResultEt.setText(resulttext);
                    progressDialog.dismiss();
                    break;

                case ERROR :
                    mResultEt.setText("텍스트 인식 실패");
                    progressDialog.dismiss();
                    break;
            }
        }
    };

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //json 파일에서 화학성분을 리스트로 만든다.
        chemicalist = new ArrayList<>();
        chemicalparse();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("이 성분 안전한가요?");

        mResultEt = findViewById(R.id.resultET);
        mPreviewIv = findViewById(R.id.imageView);
        sendbt = findViewById(R.id.send_bt);
        //camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

       /* kobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                processImage();

            }
        });*/

        sendbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                comparedlist = new ArrayList<>();
                //분석
                for (int i = 0; i< chemicalist.size() ; i++){
                    for (int j = 0 ; j < imagetotextlist.size() ; j++ ){
                        if (imagetotextlist.get(j).equals(chemicalist.get(i))){
                            comparedlist.add(imagetotextlist.get(j));
                            break;
                        }
                    }
                }
                String result = "";
                for (int i = 0 ; i < comparedlist.size() ; i++){
                    result = result + comparedlist.get(i);
                }
                Log.d(TAG, "onClick: "+result);

                if (result.equals("")){
                    Intent safeintent = new Intent(MainActivity.this, SafeActivity.class);
                    startActivity(safeintent);
                }else {
                    Intent unsafeintent = new Intent(MainActivity.this, UnSafeActivity.class);
                    unsafeintent.putExtra("result",result);
                    startActivity(unsafeintent);
                }
                mResultEt.setText(result);

            }
        });
    }

    private void chemicalparse() {
            String json = null;
            try {
                InputStream is = getAssets().open("chemical_data.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        try {
            JSONArray jsonArray = new JSONArray(json);
            Log.d(TAG, "chemicalparse: "+jsonArray.length());
            for (int i = 0 ; i<jsonArray.length();i++){

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String info = jsonObject.getString("화학물질/성분");
                chemicalist.add(info);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Process an Image
    public void processImage() {
        //google vision
        //1.API KEY
        //2.HTTP으로 요청
        //3.전송할 때 사용하는 JSON factory
        //http통신은 구글 서버와 한다.
        imagetotextlist = new ArrayList<>();
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("API KEY"));

        final Vision vision = visionBuilder.build();


        //이미지 인코딩
        // Create new thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream(image.getWidth() * image.getHeight());
                    image.compress(Bitmap.CompressFormat.PNG, 100, buffer);

                    byte[] photoData = buffer.toByteArray();
                    inputImage = new Image();
                    inputImage.encodeContent(photoData);


                    //이미지에서 텍스트 인식한다고 설정.
                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("TEXT_DETECTION");

                    //인식할 언어 설정
                    List<String> lang = new ArrayList<>();
                    lang.add("ko");
                    lang.add("en");
                    ImageContext context = new ImageContext();
                    context.setLanguageHints(lang);

                    //이미지 인식 요청
                    AnnotateImageRequest request = new AnnotateImageRequest();
                    //텍스트를 인식할 이미지
                    request.setImage(inputImage);
                    //이미지에 인식 언어 설정
                    request.setImageContext(context);
                    request.setFeatures(Arrays.asList(desiredFeature));

                    Log.d(TAG, "run:set ");

                    BatchAnnotateImagesRequest batchRequest =
                            new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Arrays.asList(request));

                    BatchAnnotateImagesResponse batchResponse = null;

                    try {
                        batchResponse = vision.images().annotate(batchRequest).execute();
                        TextAnnotation text = batchResponse.getResponses()
                                .get(0).getFullTextAnnotation();


                        resulttext = text.getText();
                        Log.d(TAG, "run: "+resulttext);
                        Message message = handler.obtainMessage();
                        message.what = KOREAN_TEXT;
                        message.obj = resulttext;
                        handler.sendMessage(message);

                        resulttext = resulttext.replace(" ",",");
                        resulttext = resulttext.replace("\n",",");
                        resulttext = resulttext.replace("(",",");
                        resulttext = resulttext.replace(")",",");

                        Log.d(TAG, "run:replace "+resulttext);
                        String[] resulttx = resulttext.split(",");
                        Log.d(TAG, "run:length "+resulttx.length);
                        for (int i = 0 ;i < resulttx.length ; i++){
                            imagetotextlist.add(resulttx[i]);
                        }

                    } catch (IOException e) {
                        Log.d(TAG, "run:error "+e.getMessage());
                        e.printStackTrace();
                    }

                // More code here
            }catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG, "run:erroror "+e.getMessage());
                    Message message = handler.obtainMessage();
                    message.what = ERROR;
                    handler.sendMessage(message);
                }
        }});
    }

    //actionbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //handle actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addImage){
            showImageImportDialog();
        }
        if (id == R.id.settings){
            Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog() {
        //items to display in dialog
        String[] items = {" Camera", " Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //set title
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which ==0){
                    //camera option click
                    if (!checkCameraPermission()){
                        //camera permission not allowed, request it
                        requestCamerapermission();
                    }else {
                        //permission allowed
                        pickCamera();
                    }
                }

                if (which ==1){
                    //gallery option click
                    if (!checkStoragePermission()){
                        //camera permission not allowed, request it
                        requestStoragepermission();
                    }else {
                        //permission allowed
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void pickGallery() {
        //갤러리에서 이미지를 가져옴..
        Intent intent = new Intent(Intent.ACTION_PICK);
        //set Intent type
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        // 카메라에서 찍은 사진을 외부 경로에 저장한다.
        // 고퀄에 이미지를 얻기 위해서.
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic"); //이미지 이름
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text"); // 이미지 설명
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragepermission() {
        ActivityCompat.requestPermissions(this, storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCamerapermission() {
        ActivityCompat.requestPermissions(this, cameraPermission,CAMERA_REQUEST_CODE);
    }

    //카메라랑 외부 저장소 경로 권한에 대해서.
    //고퀄의 이미지를 위해서 카메라 찍으면 이미지 뷰에 가기 전에
    //외부 저장소에 먼저 저장함.?? 근거는 노.
    private boolean checkCameraPermission() {
        boolean cameraresult = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageresult = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return cameraresult && storageresult;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length >0){
                    boolean cameraaccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (cameraaccepted && writeStorageaccepted){
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this, "권한 거부", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length >0){
                    boolean writeStorageaccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted){
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this, "권한 거부", Toast.LENGTH_SHORT).show();
                    }
                }
        }

    }

    //handle image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //이미지를 가져옴
        if (resultCode ==RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //이미지 갤러리에서 가져오고 지금 크롭할 예정
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //카메라에서 이미지 찍고 지금 크롭한다.
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        //크롭 이미지 얻기
        if (requestCode ==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode ==RESULT_OK){
                Uri resultUri = result.getUri(); //이미지 uri 획득
                //set image to imgview
                mPreviewIv.setImageURI(resultUri);

                //글자 인식을 위해 이미지 비트맵 얻기
                BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                image = bitmap;

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if (!recognizer.isOperational()){
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();

                    //get text from sb 텍스트가 없을 때 까지.
                    for (int i = 0; i<items.size() ; i++){
                        Log.d(TAG, "onActivityResult:한개씩 텍스트 "+items.valueAt(i).getValue());
                        TextBlock myitem = items.valueAt(i);
                        Log.d(TAG, "onActivityResult:lang "+myitem.getLanguage());
                        sb.append(myitem.getValue());
                        sb.append("\n");
                    }
                    Log.d(TAG, "onActivityResult:결과 텍스트"+sb.toString());
                    //인식된 텍스트를 사용자에게 보여줌
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle("글자 인식 중");
                    progressDialog.setMessage("이미지에서 글자 인식 중 입니다.");
                    progressDialog.show();
                    processImage();
                }
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                //크롭중에 에러가 있다면
                Exception error = result.getError();
                Toast.makeText(this, "error : "+error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
