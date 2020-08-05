package com.example.urineanalysis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Area;
import com.anychart.core.cartesian.series.Line;
import com.anychart.core.ui.Crosshair;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.MarkerType;
import com.anychart.enums.ScaleStackMode;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.example.urineanalysis.utils.FolderUtil;

import java.util.ArrayList;
import java.util.List;

public class menu_rbc extends AppCompatActivity {
    ArrayList<Double> list_rb=new ArrayList<>();
    public final String TAG="Chart2Activity_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_rbc);

        AnyChartView anyChartView = findViewById(R.id.any_chart_rbc);


        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                // TODO ystroke
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("소변검사그래프");

        cartesian.yAxis(0).title("Concentration (mg/dL)");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);




        //{{{차트에 사용할 데이터 전처리
        Set set = Set.instantiate();
        FolderUtil folderUtil=new FolderUtil();
        List<DataEntry> seriesData=new ArrayList<>();
        try {

            list_rb =folderUtil.fileRead("rb.txt");



            for(int i=0;i<list_rb.size();i++){
                seriesData.add(new CustomDataEntry(String.format("%d차 측정",i+1),list_rb.get(i)));
            }
//            seriesData.add(new CustomDataEntry(String.format("%d주",1),list_gloucose.get(0),list_protein.get(1), list_rbc.get(2),list_ph.get(3)));
            set.data(seriesData);

        }catch(Exception e){
            e.printStackTrace();
        }
        //차트에 사용할 데이터 전처리}}}







        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");


        Line series1 = cartesian.line(series1Mapping);
        series1.name("RBC");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        anyChartView.setChart(cartesian);
    }

    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value) {
            super(x, value);

        }

    }
}
