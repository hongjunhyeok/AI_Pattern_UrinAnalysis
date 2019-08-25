package com.example.urineanalysis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    final String TAG= "ResultActivity_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_result);
        anyChartView_G = findViewById(R.id.rst_chart_view_gloucose);
        anyChartView_P = findViewById(R.id.rst_chart_view_protein);
        anyChartView_B = findViewById(R.id.rst_chart_view_bilirubin);
        anyChartView_U = findViewById(R.id.rst_chart_view_urobilinogen);

        anyChartView_G.setProgressBar(findViewById(R.id.rst_progressBar));


        FolderUtil folderUtil = new FolderUtil();
        List<DataEntry> seriesData = new ArrayList<>();
        try {

            list_bilirubin = folderUtil.fileRead("bilirubin.txt");
            list_gloucose = folderUtil.fileRead("gloucose.txt");
            list_protein = folderUtil.fileRead("protein.txt");
            list_urobilinogen = folderUtil.fileRead("urobilinogen.txt");
            Log.i(TAG, Integer.toString(list_urobilinogen.size()));


        } catch (Exception e) {
            e.printStackTrace();
        }
        //차트에 사용할 데이터 전처리}}}


        draw_g();
        draw_p();
        draw_b();
        draw_u();
    }

    public void draw_g(){
        AnyChartView anyChartView = findViewById(R.id.rst_chart_view_gloucose);
        APIlib.getInstance().setActiveAnyChartView(anyChartView);

        LinearGauge linearGauge1 = AnyChart.linear();

        double tmp=0.0;
        for (int i = 0; i < list_gloucose.size(); i++) {
            tmp += list_gloucose.get(i);
        }


        linearGauge1.data(new SingleValueDataSet(new Double[]{tmp / list_gloucose.size()}));


        linearGauge1.layout(Layout.HORIZONTAL);


        //제목
        linearGauge1.label(0)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("-50px")
                .offsetX("50px")
                .fontColor("black")
                .fontSize(17);
        linearGauge1.label(0).text("포도당");


        //좌 문구
        linearGauge1.label(1)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge1.label(1).text("정상");


        //우 문구
        linearGauge1.label(2)
                .position(Position.RIGHT_CENTER)
                .anchor(Anchor.RIGHT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge1.label(2).text("검진 필요");


        OrdinalColor scaleBarColorScale = OrdinalColor.instantiate();
        scaleBarColorScale.ranges(new String[]{
                "{ from: 0, to: 30, color: ['#2AD62A', '#CAD70b'] }",
                "{ from: 30, to: 50, color: ['#CAD70b','#EB7A02'] }",
                "{ from: 50, to: 100, color: ['#EB7A02','#D81E05'] }"

        });

        linearGauge1.scaleBar(0)
                .width("5%")

                .colorScale(scaleBarColorScale);

        linearGauge1.marker(0)
                .type(MarkerType.TRIANGLE_DOWN)
                .color("red")
                .offset("-3.5%") //y위치
                .zIndex(50);


        linearGauge1.scale()
                .minimum(0)
                .maximum(100);
//        linearGauge.scale().ticks

        linearGauge1.axis(0)
                .minorTicks(false)
                .width("1%");
        linearGauge1.axis(0)
                .offset("-1.5%")
                .orientation(Orientation.TOP)
                .labels("top");

        linearGauge1.padding(0, 30, 0, 30);

        linearGauge1.tooltip().title(false);
        linearGauge1.tooltip().separator(false);
        linearGauge1.tooltip().format("VALUE : 5");


        anyChartView.setChart(linearGauge1);
    }
    public void draw_b(){
        AnyChartView anyChartView3 = findViewById(R.id.rst_chart_view_bilirubin);
        APIlib.getInstance().setActiveAnyChartView(anyChartView3);

        LinearGauge linearGauge3 = AnyChart.linear();

        double tmp=0.0;
        for (int i = 0; i < list_bilirubin.size(); i++) {
            tmp += list_bilirubin.get(i);
        }


        linearGauge3.data(new SingleValueDataSet(new Double[]{tmp / list_bilirubin.size()}));


        linearGauge3.layout(Layout.HORIZONTAL);


        //제목
        linearGauge3.label(0)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("-50px")
                .offsetX("50px")
                .fontColor("black")
                .fontSize(17);
        linearGauge3.label(0).text("빌리루빈");


        //좌 문구
        linearGauge3.label(1)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge3.label(1).text("정상");


        //우 문구
        linearGauge3.label(2)
                .position(Position.RIGHT_CENTER)
                .anchor(Anchor.RIGHT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge3.label(2).text("검진 필요");


        OrdinalColor scaleBarColorScale = OrdinalColor.instantiate();
        scaleBarColorScale.ranges(new String[]{
                "{ from: 0, to: 30, color: ['#2AD62A', '#CAD70b'] }",
                "{ from: 30, to: 50, color: ['#CAD70b','#EB7A02'] }",
                "{ from: 50, to: 100, color: ['#EB7A02','#D81E05'] }"

        });

        linearGauge3.scaleBar(0)
                .width("5%")

                .colorScale(scaleBarColorScale);

        linearGauge3.marker(0)
                .type(MarkerType.TRIANGLE_DOWN)
                .color("red")
                .offset("-3.5%") //y위치
                .zIndex(50);


        linearGauge3.scale()
                .minimum(0)
                .maximum(100);
//        linearGauge.scale().ticks

        linearGauge3.axis(0)
                .minorTicks(false)
                .width("1%");
        linearGauge3.axis(0)
                .offset("-1.5%")
                .orientation(Orientation.TOP)
                .labels("top");

        linearGauge3.padding(0, 30, 0, 30);

        linearGauge3.tooltip().title(false);
        linearGauge3.tooltip().separator(false);
        linearGauge3.tooltip().format("VALUE : 5");


        anyChartView3.setChart(linearGauge3);
    }
    public void draw_u(){
        AnyChartView anyChartView4 = findViewById(R.id.rst_chart_view_urobilinogen);
        APIlib.getInstance().setActiveAnyChartView(anyChartView4);




        LinearGauge linearGauge4=AnyChart.linear();

        double tmp=0.0;

        for (int i = 0; i < list_urobilinogen.size(); i++) {
            tmp += list_urobilinogen.get(i);
        }

        linearGauge4.data(new SingleValueDataSet(new Double[]{tmp / list_urobilinogen.size()}));
        linearGauge4.layout(Layout.HORIZONTAL);

        //제목
        linearGauge4.label(0)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("-50px")
                .offsetX("50px")
                .fontColor("black")
                .fontSize(17);
        linearGauge4.label(0).text("우로빌리노겐");
        //좌 문구
        linearGauge4.label(1)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge4.label(1).text("정상");


        //우 문구
        linearGauge4.label(2)
                .position(Position.RIGHT_CENTER)
                .anchor(Anchor.RIGHT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge4.label(2).text("검진 필요");


        OrdinalColor scaleBarColorScale = OrdinalColor.instantiate();
        scaleBarColorScale.ranges(new String[]{
                "{ from: 0, to: 30, color: ['#2AD62A', '#CAD70b'] }",
                "{ from: 30, to: 50, color: ['#CAD70b','#EB7A02'] }",
                "{ from: 50, to: 100, color: ['#EB7A02','#D81E05'] }"

        });

        linearGauge4.scaleBar(0)
                .width("5%")

                .colorScale(scaleBarColorScale);

        linearGauge4.marker(0)
                .type(MarkerType.TRIANGLE_DOWN)
                .color("red")
                .offset("-3.5%") //y위치
                .zIndex(50);


        linearGauge4.scale()
                .minimum(0)
                .maximum(100);
//        linearGauge.scale().ticks

        linearGauge4.axis(0)
                .minorTicks(false)
                .width("1%");
        linearGauge4.axis(0)
                .offset("-1.5%")
                .orientation(Orientation.TOP)
                .labels("top");

        linearGauge4.padding(0, 30, 0, 30);

        linearGauge4.tooltip().title(false);
        linearGauge4.tooltip().separator(false);
        linearGauge4.tooltip().format("VALUE : 5");



        anyChartView4.setChart(linearGauge4);
    }

    public void draw_p(){
        AnyChartView anyChartView2 = findViewById(R.id.rst_chart_view_protein);
        APIlib.getInstance().setActiveAnyChartView(anyChartView2);




        LinearGauge linearGauge2=AnyChart.linear();

        double tmp=0.0;

        for (int i = 0; i < list_protein.size(); i++) {
            tmp += list_protein.get(i);
        }

        linearGauge2.data(new SingleValueDataSet(new Double[]{tmp / list_protein.size()}));
        linearGauge2.layout(Layout.HORIZONTAL);

        //제목
        linearGauge2.label(0)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("-50px")
                .offsetX("50px")
                .fontColor("black")
                .fontSize(17);
        linearGauge2.label(0).text("단백질");
        //좌 문구
        linearGauge2.label(1)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge2.label(1).text("정상");


        //우 문구
        linearGauge2.label(2)
                .position(Position.RIGHT_CENTER)
                .anchor(Anchor.RIGHT_CENTER)
                .offsetY("40px")
                .offsetX("50px")
                .fontColor("#777777")
                .fontSize(17);
        linearGauge2.label(2).text("검진 필요");


        OrdinalColor scaleBarColorScale = OrdinalColor.instantiate();
        scaleBarColorScale.ranges(new String[]{
                "{ from: 0, to: 30, color: ['#2AD62A', '#CAD70b'] }",
                "{ from: 30, to: 50, color: ['#CAD70b','#EB7A02'] }",
                "{ from: 50, to: 100, color: ['#EB7A02','#D81E05'] }"

        });

        linearGauge2.scaleBar(0)
                .width("5%")

                .colorScale(scaleBarColorScale);

        linearGauge2.marker(0)
                .type(MarkerType.TRIANGLE_DOWN)
                .color("red")
                .offset("-3.5%") //y위치
                .zIndex(50);


        linearGauge2.scale()
                .minimum(0)
                .maximum(100);
//        linearGauge.scale().ticks

        linearGauge2.axis(0)
                .minorTicks(false)
                .width("1%");
        linearGauge2.axis(0)
                .offset("-1.5%")
                .orientation(Orientation.TOP)
                .labels("top");

        linearGauge2.padding(0, 30, 0, 30);

        linearGauge2.tooltip().title(false);
        linearGauge2.tooltip().separator(false);
        linearGauge2.tooltip().format("VALUE : 5");



        anyChartView2.setChart(linearGauge2);
    }
}


