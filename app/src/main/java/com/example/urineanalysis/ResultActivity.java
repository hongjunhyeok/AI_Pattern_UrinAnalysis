package com.example.urineanalysis;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.SingleValueDataSet;
import com.anychart.charts.LinearGauge;
import com.anychart.enums.Anchor;
import com.anychart.enums.Layout;
import com.anychart.enums.MarkerType;
import com.anychart.enums.Orientation;
import com.anychart.enums.Position;
import com.anychart.scales.OrdinalColor;
import com.example.urineanalysis.utils.FolderUtil;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    Bitmap image;
    Mat mRgba;
    TextView tv;
    ImageView BigImate;
    AnyChartView anyChartView_G,anyChartView_P,anyChartView_B, anyChartView_H;

    ArrayList<Double>list_gloucose=new ArrayList<>();
    ArrayList<Double>list_protein=new ArrayList<>();
    ArrayList<Double> list_rbc =new ArrayList<>();
    ArrayList<Double> list_ph =new ArrayList<>();

    final String TAG= "ResultActivity_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_result);
        anyChartView_G = findViewById(R.id.rst_chart_view_gloucose);
        anyChartView_P = findViewById(R.id.rst_chart_view_protein);
        anyChartView_B = findViewById(R.id.rst_chart_view_rbc);
        anyChartView_H = findViewById(R.id.rst_chart_view_ph);

        anyChartView_G.setProgressBar(findViewById(R.id.rst_progressBar));


        FolderUtil folderUtil = new FolderUtil();
        List<DataEntry> seriesData = new ArrayList<>();
        try {

            list_rbc = folderUtil.fileRead("result.txt");
            list_gloucose = folderUtil.fileRead("result.txt");
            list_protein = folderUtil.fileRead("result.txt");
            list_ph = folderUtil.fileRead("result.txt");
//            Log.i(TAG, Integer.toString(list_ph.size()));


        } catch (Exception e) {
            e.printStackTrace();
        }
        //차트에 사용할 데이터 전처리}}}

        Log.i(TAG,String.format("%.2f %.2f %.2f %.2f",list_gloucose.get(0),list_gloucose.get(1),list_gloucose.get(2),list_gloucose.get(3)));
        draw_g();
        draw_p();
        draw_r();
        draw_h();
    }

    public void draw_g(){
        AnyChartView anyChartView = findViewById(R.id.rst_chart_view_gloucose);
        APIlib.getInstance().setActiveAnyChartView(anyChartView);

        LinearGauge linearGauge1 = AnyChart.linear();

        double tmp=0.0;
//        for (int i = 0; i < list_gloucose.size(); i++) {
//            tmp += list_gloucose.get(i);
//        }
        tmp=list_gloucose.get(0);



        linearGauge1.data(new SingleValueDataSet(new Double[]{tmp}));


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
    public void draw_r(){
        AnyChartView anyChartView3 = findViewById(R.id.rst_chart_view_rbc);
        APIlib.getInstance().setActiveAnyChartView(anyChartView3);

        LinearGauge linearGauge3 = AnyChart.linear();

        double tmp=0.0;
//        for (int i = 0; i < list_rbc.size(); i++) {
//            tmp += list_rbc.get(i);
//        }

        tmp=list_rbc.get(2);
        linearGauge3.data(new SingleValueDataSet(new Double[]{tmp}));


        linearGauge3.layout(Layout.HORIZONTAL);


        //제목
        linearGauge3.label(0)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("-50px")
                .offsetX("50px")
                .fontColor("black")
                .fontSize(17);
        linearGauge3.label(0).text("RBC(적혈구)");


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
    public void draw_h(){
        AnyChartView anyChartView4 = findViewById(R.id.rst_chart_view_ph);
        APIlib.getInstance().setActiveAnyChartView(anyChartView4);




        LinearGauge linearGauge4=AnyChart.linear();

        double tmp=0.0;

//        for (int i = 0; i < list_ph.size(); i++) {
//            tmp += list_ph.get(i);
//        }
        tmp=list_ph.get(3);
        linearGauge4.data(new SingleValueDataSet(new Double[]{tmp}));
        linearGauge4.layout(Layout.HORIZONTAL);

        //제목
        linearGauge4.label(0)
                .position(Position.LEFT_CENTER)
                .anchor(Anchor.LEFT_CENTER)
                .offsetY("-50px")
                .offsetX("50px")
                .fontColor("black")
                .fontSize(17);
        linearGauge4.label(0).text("PH");
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

//        for (int i = 0; i < list_protein.size(); i++) {
//            tmp += list_protein.get(i);
//        }

        tmp=list_protein.get(1);

        linearGauge2.data(new SingleValueDataSet(new Double[]{tmp}));
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

        //수거목록 :wifi- 인터넷 모뎀기, TV 세탑박스랑 리모컨.

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


