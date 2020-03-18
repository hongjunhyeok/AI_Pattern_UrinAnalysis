package com.example.urineanalysis;

import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;


import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Area;
import com.anychart.core.ui.Crosshair;
import com.anychart.data.Set;

import com.anychart.data.Mapping;
import com.anychart.enums.HoverMode;
import com.anychart.enums.MarkerType;
import com.anychart.enums.ScaleComparisonMode;
import com.anychart.enums.ScaleStackMode;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.graphics.vector.Stroke;
import com.example.urineanalysis.utils.FolderUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Chart2Activity extends AppCompatActivity {

    ArrayList<Double>list_gloucose=new ArrayList<>();
    ArrayList<Double>list_protein=new ArrayList<>();
    ArrayList<Double>list_bilirubin=new ArrayList<>();
    ArrayList<Double>list_urobilinogen=new ArrayList<>();
    public final String TAG="Chart2Activity_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_chart2);

        //{{{차트에 사용할 데이터 전처리
        Set set = Set.instantiate();
        FolderUtil folderUtil=new FolderUtil();
        List<DataEntry> seriesData=new ArrayList<>();
        List<String> dayData=new ArrayList<>();
        List<String> IndexData=new ArrayList<>();

        String line="";

        try {
            FileReader reader=new FileReader(Environment.getExternalStorageDirectory() + "/OneFileUrineCup/gloucose.txt");
            BufferedReader rd1 =new BufferedReader(reader);


            while((line =rd1.readLine()) != null){

                String[] lineFormat= line.split("-");
                Log.i(TAG,lineFormat[1]);
                IndexData.add(lineFormat[0]);
                dayData.add(lineFormat[1]);

            }


            list_bilirubin=folderUtil.fileRead("bilirubin.txt");
            list_gloucose=folderUtil.fileRead("gloucose.txt");
            list_protein=folderUtil.fileRead("protein.txt");
            list_urobilinogen=folderUtil.fileRead("urobilinogen.txt");
            Log.i(TAG,Integer.toString(list_urobilinogen.size()));

            if(list_urobilinogen.size()<=7) {
                for (int i = 0; i < list_urobilinogen.size(); i++) {
                    seriesData.add(new CustomDataEntry(IndexData.get(i) + "/" + dayData.get(i), list_gloucose.get(i), list_protein.get(i), list_bilirubin.get(i), list_urobilinogen.get(i)));
//                seriesData.add(new CustomDataEntry(String.format("%d주",i+1),1,2,3,4));

                }
            }else{
                for (int i = list_urobilinogen.size()-7; i < list_urobilinogen.size(); i++) {
                    seriesData.add(new CustomDataEntry(IndexData.get(i) + "/" + dayData.get(i), list_gloucose.get(i), list_protein.get(i), list_bilirubin.get(i), list_urobilinogen.get(i)));
//                seriesData.add(new CustomDataEntry(String.format("%d주",i+1),1,2,3,4));

                }
            }
            set.data(seriesData);

        }catch(Exception e){
            e.printStackTrace();
            Log.i(TAG,e.toString());
        }
        //차트에 사용할 데이터 전처리}}}




        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        Cartesian areaChart = AnyChart.area();

        areaChart.animation(true);

        Crosshair crosshair = areaChart.crosshair();
        crosshair.enabled(true);

        crosshair.yStroke((Stroke) null, null, null, (String) null, (String) null)
                .xStroke("#fff", 1d, null, (String) null, (String) null)
                .zIndex(10d);
        crosshair.yLabel(0).enabled(true);
//        areaChart.yScale().comparisonMode(ScaleComparisonMode.VALUE);
//        areaChart.yScale().comparisonMode(ScaleComparisonMode.VALUE);
        areaChart.yScale().stackMode(ScaleStackMode.VALUE);

        areaChart.title("소변시험지 결과분석표");




        Mapping series1Data = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Data = set.mapAs("{ x: 'x', value: 'value2' }");
        Mapping series3Data = set.mapAs("{ x: 'x', value: 'value3' }");
        Mapping series4Data = set.mapAs("{ x: 'x', value: 'value4' }");

        Area series1 = areaChart.area(series1Data);
        series1.name("포도당");
        series1.stroke("3 #fff");
        series1.hovered().stroke("3 #fff");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(3d)
                .stroke("1.5 #fff");
        series1.markers().zIndex(100d);

        Area series2 = areaChart.area(series2Data);
        series2.name("단백질");
        series2.stroke("3 #fff");
        series2.hovered().stroke("3 #fff");
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(3d)
                .stroke("1.5 #fff");
        series2.markers().zIndex(100d);

        Area series3 = areaChart.area(series3Data);
        series3.name("빌리루빈");
        series3.stroke("3 #fff");
        series3.hovered().stroke("3 #fff");
        series3.hovered().markers().enabled(true);
        series3.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(3d)
                .stroke("1.5 #fff");
        series3.markers().zIndex(100d);

        Area series4 = areaChart.area(series4Data);
        series4.name("우로빌리노겐");
        series4.stroke("3 #fff");
        series4.hovered().stroke("3 #fff");
        series4.hovered().markers().enabled(true);
        series4.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(3d)
                .stroke("1.5 #fff");
        series4.markers().zIndex(100d);



        areaChart.legend().enabled(true);
        areaChart.legend().fontSize(13d);
        areaChart.legend().padding(0d, 0d, 20d, 0d);

        areaChart.xAxis(0).title(false);
        areaChart.yAxis(0).title("농도(100ml당 단위mg)");

        areaChart.interactivity().hoverMode(HoverMode.BY_X);
        areaChart.tooltip()
                .background(true)
                .valuePrefix("농도")
                .valuePostfix(" mg/dL.")
                .displayMode(TooltipDisplayMode.UNION);

        anyChartView.setChart(areaChart);


    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2, Number value3, Number value4) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
            setValue("value4", value4);

        }
    }
}
