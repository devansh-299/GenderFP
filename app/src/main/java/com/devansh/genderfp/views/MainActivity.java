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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    @BindView(R.id.ll_get_image)
    LinearLayout getImageLayout;

    @BindView(R.id.ll_show_result)
    LinearLayout showResultLayout;

    @BindView(R.id.iv_result_gender)
    ImageView resultGenderImageView;

    @BindView(R.id.tv_result_gender)
    TextView tvResultGender;

    Animation fadeInAnim;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 299;
    Bitmap imageBitmap;
    Bitmap tempBitmap;
    private static final int INPUT_SIZE = 224;
    public static final String MODEL_PATH = "fingerprint_mobilenet.tflite";
    private static final String LABEL_PATH = "labels.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fadeInAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in_anim);

        getImageLayout.setVisibility(View.VISIBLE);
        showResultLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.clickphoto)
    public void getPhoto () {
        bQuestion.setVisibility(View.VISIBLE);
        ivClick.startAnimation(fadeInAnim);
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
       bQuestion.startAnimation(fadeInAnim);
        try {
        Classifier myClassifier = new Classifier(getAssets(),
                MODEL_PATH, LABEL_PATH,
                INPUT_SIZE);
        List<Classifier.Recognition> result = myClassifier.
                recognizeImage(tempBitmap);
            String classifierResult = result.get(0).getId();
            String confidence = Float.toString(result.get(0).getConfidence());
            Toast.makeText(this,"with "+confidence+" confidence",
                    Toast.LENGTH_LONG).show();
            updateUi(classifierResult);
        } catch (Exception e) {
            if(e instanceof IllegalArgumentException){
                Toast.makeText(this, R.string.toast_message,Toast.LENGTH_SHORT).show();
            }
            Log.i("Classifier Error",e.getMessage());
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
            bQuestion.setBackgroundResource(R.drawable.button_background_image);
            tempBitmap = imageBitmap;
            ImageLoader il = new ImageLoader(
                    ivClick,
                    imageBitmap,
                    this);
            il.glideSetter();
        }
    }

    private void updateUi(String result) {
        getImageLayout.setVisibility(View.GONE);
        showResultLayout.setVisibility(View.VISIBLE);
        String resultGender = getString(R.string.male);
        if(result.equals("1")){
            resultGender = getString(R.string.female);
            resultGenderImageView.setImageResource(R.drawable.female_result);
        }
        tvResultGender.setText(getString(R.string.supporting_result_string)+" "+resultGender);
    }

    @OnClick(R.id.bt_retry)
    public void retry() {
        getImageLayout.setVisibility(View.VISIBLE);
        showResultLayout.setVisibility(View.GONE);
        bQuestion.setBackgroundResource(R.drawable.tap_above);
    }
}
