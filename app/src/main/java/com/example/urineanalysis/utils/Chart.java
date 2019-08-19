package com.example.urineanalysis.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


import com.example.urineanalysis.AnalysisActivity;
import com.example.urineanalysis.MainActivity;
import com.example.urineanalysis.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;


/**
 * Created by ygyg331 on 2018-06-28.
 */
public class Chart extends Activity {

    private static final String TAG = "Chart _TAG"; // for loggin success or failure messages
    Intent get = getIntent();


    Button btn_backToMenu;
    int loc=0;

    double[] stage={0,1,2,3,4};
    ArrayList<Double> list =new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart);

        btn_backToMenu=findViewById(R.id.btn_backToMenu);
        btn_backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_backToMenu = new Intent(getApplication(), MainActivity.class);
                intent_backToMenu.putExtra("isTrue","data-get");
                startActivity(intent_backToMenu);
            }
        });

        double hueValue=0;
        double ind=0;

        Intent geti=getIntent();
        try {
           double[] stages = geti.getExtras().getDoubleArray("chart_urobilinogen");

            for(int x=0;x<stages.length;x++){
                Log.i(TAG,Double.toString(stages[x]));
            }
           stage=stages;
        }catch (Exception e){
            e.printStackTrace();
        }
        for(int x=0;x<stage.length;x++){
            Log.i(TAG,Double.toString(stage[x]));
        }

        loc=(int)ind;

//        Log.i("data : ",Integer.toString(data[10]));

        //갯수 추가를 위해서는
        //dataset에 있는 xySeries의 수와 renderer에 있는 xySeriesRender의 수가 맞아야함
        //그래프 생성
        double[] huehue={hueValue,ind};

//        try {
            final GraphicalView chart = ChartFactory.getLineChartView(this, getDataset(stage,hueValue), getRenderer());

            //레이아웃에 추가
            LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
            layout.addView(chart);
//        layout.addView(chart2);
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.e(TAG,e.toString());
//        }

    }

    //그래프 설정 모음
    // http://www.programkr.com/blog/MQDN0ADMwYT3.html ( 그래프 설정 속성 한글로 써져있는 사이트 )
    private void setChartSettings(XYMultipleSeriesRenderer renderer) {
        //타이틀, x,y축 글자
        renderer.setChartTitle("Chart demo");
        renderer.setXTitle("단 계");


        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);
        renderer.setYTitle("Hue Value",0);

        renderer.setRange(new double[] {0,6,-70,400});


        //background
        renderer.setApplyBackgroundColor(true);      //변경 가능여부
        renderer.setBackgroundColor(Color.WHITE);    //그래프 부분 색
        renderer.setMarginsColor(Color.WHITE);       //그래프 바깥 부분 색(margin)

        //글자크기
        renderer.setAxisTitleTextSize(50);          //x,y축 title
        renderer.setChartTitleTextSize(20);         //상단
        renderer.setLabelsTextSize(15);             //x,y축 수치
        renderer.setLegendTextSize(15);             //Series 구별 글씨 크기
        renderer.setPointSize(10f);
        renderer.setMargins(new int[] { 20, 20, 50, 50 }); //Top Left Buttom Right ( '하' 의 경우 setFitLegend(true)일 때에만 가능 )

        //색
        renderer.setAxesColor(Color.RED);       //x,y축 선 색
        renderer.setLabelsColor(Color.CYAN);    //x,y축 글자색

        //x,y축 표시 간격 ( 각 축의 범위에 따라 나눌 수 있는 최소치가 제한 됨 )
        renderer.setXLabels(10);
        renderer.setYLabels(10);

        int size=stage.length;
        int max=4;
        //x축 최대 최소(화면에 보여질)
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(size);
        //y축 최대 최소(화면에 보여질)
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(max);
        renderer.setZoomEnabled(true);
        //클릭 가능 여부
        renderer.setClickEnabled(true);
        //줌 기능 가능 여부
        renderer.setZoomEnabled(true,true);
        //X,Y축 스크롤
        renderer.setPanEnabled(true, false);                // 가능 여부
        renderer.setPanLimits(new double[]{-2,24,20,40} );   // 가능한 범위

        //지정된 크기에 맞게 그래프를 키움
        renderer.setFitLegend(true);
        //간격에 격자 보이기
//        renderer.setShowGrid(true);
        renderer.setXLabelsPadding(50);
//
        renderer.setDisplayValues(true);

    }



    //선 그리기
    private XYMultipleSeriesRenderer getRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        //---그려지는 점과 선 설정----
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.BLACK);            //색
        r.setPointStyle(PointStyle.POINT);//점의 모양
        r.setFillPoints(true);//점 체우기 여부
        renderer.addSeriesRenderer(r);

        XYSeriesRenderer r2= new XYSeriesRenderer();
        r2.setColor(Color.RED);            //색
        r2.setPointStyle(PointStyle.POINT);//점의 모양
        r2.setFillPoints(false);
        r2.setFillPoints(true);//점 체우기 여부
        r2.setPointStyle(PointStyle.SQUARE);
        renderer.addSeriesRenderer(r2);
        //----------------------------

        /*
         * 다른 그래프를 추가하고 싶으면
         * XYSeriesRenderer 추가로 생성한 후
         *  renderer.addSeriesRenderer(r) 해준다 (Data도 있어야함)
         *
         */

        setChartSettings(renderer);
        return renderer;
    }


    //데이터들
    private XYMultipleSeriesDataset getDataset( double[] data,double hue ) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();


        XYSeries series = new XYSeries("평균RGB value");
        for (int i = 0; i < data.length; i++ ) {
            series.add(i, stage[i] );
        }

        XYSeries series2 =new XYSeries("hue");
        for (int i=0;i<1;i++){
            series2.add(4.5,hue);
        }
        dataset.addSeries(series);

        dataset.addSeries(series2);
        /*
         *
         * 다른 그래프를 추가하고 싶으면
         * XYSeries를 추가로 생성한 후
         * dataset.addSeries(series) 해준다 (renderer도 있어야함)
         *
         */




        return dataset;
    }

}
