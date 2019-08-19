package com.example.urineanalysis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class ResultActivity extends AppCompatActivity {

    Bitmap image;
    Mat mRgba;
    TextView tv;
    ImageView BigImate;


    final String TAG= "ResultActivity_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        byte[] arr=getIntent().getByteArrayExtra("image");
        String hue_urobilinogen=getIntent().getStringExtra("hue_urobilinogen");
        try {
            image = BitmapFactory.decodeByteArray(arr, 0, arr.length);
            BigImate = (ImageView) findViewById(R.id.ImageView_result);
            tv = findViewById(R.id.textView_result);
            BigImate.setImageBitmap(image);
            tv.setText(hue_urobilinogen);



        }catch(Exception e){
            Log.i(TAG,e.toString());
        }


    }


}
