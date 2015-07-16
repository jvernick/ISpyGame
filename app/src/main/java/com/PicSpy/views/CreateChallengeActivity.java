package com.picspy.views;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.picspy.firstapp.R;

/**
 * Created by Justin12 on 7/12/2015.
 */
public class CreateChallengeActivity extends Activity {

    ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);

        imageView = (ImageView)findViewById(R.id.imageView);
            byte[] imageData = getIntent().getExtras().getByteArray("PictureTaken");

            // Convert the byte array to a bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

            // rotate the image accordingly
        //ExifInterface exif = new ExifInterface(bitmap);

            imageView.setImageBitmap(bitmap);
            imageView.setRotation(90);

        // TODO: fix rotation issues, resolution, and UI
        }

}
