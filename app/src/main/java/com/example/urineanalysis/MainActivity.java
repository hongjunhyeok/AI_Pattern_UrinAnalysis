package com.example.urineanalysis;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btn_analysis,btn_album,btn_chart,btn_result;
    File tempFile;

    public final int PICK_FROM_ALBUM=1111;
    public final int PICK_FROM_RESULT=2222;
    public final int PICK_FROM_CHART=3333;
    public final int PICK_FROM_ANALYSIS=4444;
    public final int PICK_FROM_ACTIVITY=5555;

    public final String TAG="MainActiviy_TAG";
    private final String fileName = Environment.getExternalStorageDirectory() + "/OneFileUrineCup/2.txt" ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        btn_analysis = findViewById(R.id.btn_analysis);
        btn_album = findViewById(R.id.btn_album);
        btn_chart = findViewById(R.id.btn_chart);
        btn_result = findViewById(R.id.btn_result);

        ImageView iv = findViewById(R.id.ImageView);


        iv.setImageResource(R.drawable.logo);
        btn_analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AnalysisActivity.class);
                startActivity(i);
            }
        });



        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, 1111);
            }
        });

        btn_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                Intent i = new Intent(getApplicationContext(), Chart2Activity.class);


                startActivity(i);


            }
        });

        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i=new Intent(getApplicationContext(),ResultActivity.class);




                startActivity(i);

            }

        });




    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         //https://black-jin0427.tistory.com/120 참고했음.


        if(resultCode!= Activity.RESULT_OK){
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }
        }


        //분석뒤 -> 앨범으로 자동이동

        if(requestCode==PICK_FROM_ALBUM) {
            Cursor cursor = null;

            try {
                Uri photoUri = data.getData();

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = {MediaStore.Images.Media.DATA};
                assert photoUri != null;    //assert :: true -> pass false-> stop & message
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImages();
        }


    }


    private void setImages() {
        ImageView iv = findViewById(R.id.ImageView);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        iv.setImageBitmap(originalBm);

    }

    FileReader fileReader=null;
    BufferedReader bufferedReader =null;
    String line =null;
    double avg=0.0;
    int counter=0;


}
