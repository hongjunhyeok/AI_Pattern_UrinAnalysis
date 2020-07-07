package com.example.urineanalysis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.SingleValueDataSet;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.LinearGauge;
import com.anychart.charts.Pie;
import com.anychart.enums.Anchor;
import com.anychart.enums.Layout;
import com.anychart.enums.MarkerType;
import com.anychart.enums.Orientation;
import com.anychart.enums.Position;
import com.anychart.scales.OrdinalColor;
import com.example.urineanalysis.utils.FolderUtil;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ResultActivity extends AppCompatActivity {

    Bitmap image;
    Mat mRgba;
    TextView tv;
    ImageView BigImate;
    AnyChartView anyChartView_G,anyChartView_P,anyChartView_B,anyChartView_U;

    ArrayList<Double>list_gloucose=new ArrayList<>();
    ArrayList<Double>list_protein=new ArrayList<>();
    ArrayList<Double>list_bilirubin=new ArrayList<>();
    ArrayList<Double>list_urobilinogen=new ArrayList<>();

    final int weight1=5, weight2=12,weight3=20,weight4=35;

    final String TAG= "ResultActivity_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       setContentView(R.layout.activity_result);


        TextView textView=findViewById(R.id.textView8);
        TextView textView1=findViewById(R.id.tv_2);
        TextView textView2=findViewById(R.id.tv_3);
        TextView textView3=findViewById(R.id.tv_4);
        TextView textView4=findViewById(R.id.tv_5);
        TextView textView5=findViewById(R.id.tv_6);
        TextView textView6=findViewById(R.id.tv_7);
        TextView textView7=findViewById(R.id.tv_8);
        TextView textView8=findViewById(R.id.tv_9);
        TextView textView9=findViewById(R.id.tv_10);
        TextView textView10=findViewById(R.id.tv_11);
        TextView textView11=findViewById(R.id.tv_12);

        Random random=new Random();
        random.nextInt(10);
        textView1.setText(String.format(Locale.KOREA,"%d",random.nextInt(10)));
        textView2.setText(String.format(Locale.KOREA,"%d",random.nextInt(10)));
        textView3.setText(String.format(Locale.KOREA,"%d",random.nextInt(10)));
        textView4.setText(String.format(Locale.KOREA,"0.%d",random.nextInt(10)));
        textView5.setText(String.format(Locale.KOREA,"%d",random.nextInt(5)));
        textView6.setText("양성");
        textView7.setText(String.format(Locale.KOREA,"%d",random.nextInt(10)));
        textView8.setText(String.format(Locale.KOREA,"%d",5));
        textView9.setText(String.format(Locale.KOREA,"%d",random.nextInt(70)+1000));
        textView10.setText(String.format(Locale.KOREA,"%d",random.nextInt(40)));
        textView11.setText(String.format(Locale.KOREA,"%d",random.nextInt(8)));


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
        });
    }



}


