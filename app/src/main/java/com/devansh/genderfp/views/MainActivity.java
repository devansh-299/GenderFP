package com.devansh.genderfp.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devansh.genderfp.R;
import com.devansh.genderfp.tflite.Classifier;
import com.devansh.genderfp.util.ImageLoader;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.clickphoto)
    ImageView ivClick;

    @BindView(R.id.question_b)
    Button bQuestion;

    @BindView(R.id.result_tv)
    TextView tvResult;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 299;
    Bitmap imageBitmap;
    Bitmap tempBitmap;
    private static final int INPUT_SIZE = 224;
    public static final String MODEL_PATH = "fingerprint.tflite";
    private static final String LABEL_PATH = "labels.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.clickphoto)
    public void getPhoto () {
        tvResult.setVisibility(View.GONE);
        bQuestion.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    @OnClick(R.id.question_b)
    public void getResults () {
        Classifier myClassifier = new Classifier(getAssets(),
                MODEL_PATH,LABEL_PATH,
                INPUT_SIZE);
        List<Classifier.Gender  > result = myClassifier.
                recognizeImage(tempBitmap);
        try {
            tvResult.setText(result.get(0).getGender());
            bQuestion.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            tempBitmap = imageBitmap;
            ImageLoader il = new ImageLoader(
                    ivClick,
                    imageBitmap,
                    this);
            il.glideSetter();
        }
    }

}
