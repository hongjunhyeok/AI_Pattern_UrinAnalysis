package com.example.urineanalysis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.SingleValueDataSet;
import com.anychart.charts.LinearGauge;
import com.anychart.enums.Anchor;
import com.anychart.enums.Layout;
import com.anychart.enums.MarkerType;
import com.anychart.enums.Orientation;
import com.anychart.enums.Position;
import com.anychart.scales.OrdinalColor;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_result);
        AnyChartView anyChartView = findViewById(R.id.rst_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.rst_progressBar));




        LinearGauge linearGauge = AnyChart.linear();

        linearGauge.data(new SingleValueDataSet(new Double[] { 3.2D }));

        linearGauge.layout(Layout.HORIZONTAL);


        //제목
        linearGauge.label(0)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("-50px")
                .offsetX("50px")
                .fontColor("black")
                .fontSize(17);
        linearGauge.label(0).text("포도당");


        //좌 문구
        linearGauge.label(1)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge.label(1).text("정상");


        //우 문구
        linearGauge.label(2)
                .position(Position.RIGHT_CENTER)
                .anchor(Anchor.RIGHT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge.label(2).text("검진 필요");



        OrdinalColor scaleBarColorScale = OrdinalColor.instantiate();
        scaleBarColorScale.ranges(new String[]{
                "{ from: 0, to: 30, color: ['green 0.5'] }",
                "{ from: 30, to: 50, color: ['yellow 0.5'] }",
                "{ from: 50, to: 100, color: ['red 0.5'] }"

        });

        linearGauge.scaleBar(0)
                .width("5%")
                .colorScale(scaleBarColorScale);

        linearGauge.marker(0)
                .type(MarkerType.TRIANGLE_DOWN)
                .color("red")
                .offset("-3.5%")
                .zIndex(100);


        linearGauge.scale()
                .minimum(0)
                .maximum(100);
//        linearGauge.scale().ticks

        linearGauge.axis(0)
                .minorTicks(false)
                .width("1%");
        linearGauge.axis(0)
                .offset("-1.5%")
                .orientation(Orientation.TOP)
                .labels("top");

        linearGauge.padding(0, 30, 0, 30);

        anyChartView.setChart(linearGauge);

    }


}
