package com.devansh.genderfp.util;

import android.content.Context;
import android.graphics.Bitmap;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.devansh.genderfp.R;

import java.io.ByteArrayOutputStream;

public class ImageLoader {
    ImageView imageView;
    Bitmap bitmap;
    Context context;

    public ImageLoader (ImageView iv, Bitmap bp, Context context) {
        this.bitmap = bp;
        this.imageView =iv;
        this.context = context;
    }

    public void glideSetter() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Glide.with(context)
                .asBitmap()
                .load(stream.toByteArray())
                .error(R.drawable.splashbackground)
                .apply(RequestOptions.circleCropTransform())
                .circleCrop()
                .into(imageView);
    }

}
