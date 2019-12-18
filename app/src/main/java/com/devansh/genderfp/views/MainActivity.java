package com.devansh.genderfp.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devansh.genderfp.R;
import com.devansh.genderfp.model.ImageInfo;
import com.devansh.genderfp.remote.Api;
import com.devansh.genderfp.remote.ApiService;

import com.devansh.genderfp.util.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.clickphoto)
    ImageView ivClick;

    @BindView(R.id.question_b)
    Button bQuestion;

    @BindView(R.id.result_tv)
    TextView tvResult;

    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;

    @BindView(R.id.main_layout)
    LinearLayout mainLayout;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 299;
    Bitmap imageBitmap;

    private static final int INPUT_SIZE = 224;
    public static final String MODEL_PATH = "fingerprint.tflite";
    private static final String LABEL_PATH = "labels.txt";

    String imagePath;
    Uri imageUri;
    ApiService apiService;
    Api api;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loadingLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.clickphoto)
    public void getPhoto() {
        tvResult.setVisibility(View.GONE);
        bQuestion.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) &&
                    (checkSelfPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

//    @OnClick(R.id.question_b)
//    public void getResults () {
//        Classifier myClassifier = new Classifier(getAssets(),
//                MODEL_PATH,LABEL_PATH,
//                INPUT_SIZE);
//        List<Classifier.Gender  > result = myClassifier.
//                recognizeImage(tempBitmap);
//        try {
//            tvResult.setText(result.get(0).getGender());
//            bQuestion.setVisibility(View.GONE);
//        } catch (Exception e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }

    @OnClick(R.id.question_b)
    public void getResults() {

        mainLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);

        // First uploading image
        File file = new File(imagePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "GenderFP.jpg", requestBody);

        apiService = new ApiService();
        api = apiService.getClient();

        if (api == null) {
            Toast.makeText(getApplicationContext(), "Api is null", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<ImageInfo> call = api.uploadImage(body);
        call.enqueue(new Callback<ImageInfo>() {
            @Override
            public void onResponse(Call<ImageInfo> call, Response<ImageInfo> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Successfully uploaded", Toast.LENGTH_SHORT).show();
                    getAnalizedResults();
                } else {
                    Toast.makeText(getApplicationContext(), "Negative Response", Toast.LENGTH_SHORT).show();
                    mainLayout.setVisibility(View.VISIBLE);
                    loadingLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ImageInfo> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void getAnalizedResults() {
        Call<String> stringCall;
        stringCall = api.getResult();
        stringCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String responseString = response.body();
                    bQuestion.setVisibility(View.GONE);
                    tvResult.setVisibility(View.VISIBLE);
                    mainLayout.setVisibility(View.VISIBLE);
                    loadingLayout.setVisibility(View.GONE);
                    if (responseString.equals("0")) {
                        tvResult.setText("Nice! According to us your gender is Male");
                        ivClick.setImageDrawable(getResources().getDrawable(R.drawable.male_result));
                    } else {
                        tvResult.setText("Nice! According to us your gender is Female");
                        ivClick.setImageDrawable(getResources().getDrawable(R.drawable.male_result));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Unsuccessful getting result", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        R.string.camera_permission_granted,
                        Toast.LENGTH_LONG)
                        .show();
                Intent cameraIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this,
                        R.string.camera_permission_denied,
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            imageBitmap = (Bitmap) data.getExtras()
                    .get(getString(R.string.data));
            ImageLoader il = new ImageLoader(
                    ivClick,
                    imageBitmap,
                    this);

            il.glideSetter();
            imageUri = getImageUri(getApplicationContext(), imageBitmap);
            imagePath = getRealPathFromUri(imageUri);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private String getRealPathFromUri(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Empty uri", Toast.LENGTH_SHORT).show();
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

}
