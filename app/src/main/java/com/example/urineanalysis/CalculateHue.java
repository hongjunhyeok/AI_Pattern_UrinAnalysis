package com.example.urineanalysis;

import android.util.Log;

import org.opencv.core.Point;

import java.util.HashMap;

/**
 * Created by ygyg331 on 2018-10-24.
 */

public class CalculateHue {


    //R = r*100 / r+g+b , G = g*100 / r+g+b, B = b*100 / r+g+b
    private double[] getLargeRGB(double[] value){



            double H = 0.0;
            // index 0 1 2  : R G B
            double[] LargeRGB = new double[3];
        try {
            double sum = value[0] + value[1] + value[2];
            LargeRGB[0] = value[0] * 100 / sum;
            LargeRGB[1] = value[1] * 100 / sum;
            LargeRGB[2] = value[2] * 100 / sum;
        }catch(Exception e){
            e.printStackTrace();
        }

        return LargeRGB;
    };

    public double getS(double[] value){

        double[] tmpRGB=getLargeRGB(value);
        double S=0.0;
        double tmp=Math.min(tmpRGB[0],tmpRGB[1]);
        double minValue= Math.min(tmpRGB[2],tmp);
        S= 1-(tmpRGB[0]+tmpRGB[1]+tmpRGB[2])/3 * minValue;

        return S;
    };

    public double getI(double[] value){
        double[] tmpRGB=getLargeRGB(value);

        double I=(tmpRGB[0]+tmpRGB[1]+tmpRGB[2])/3;

        return I;
    };

    //H = arccos( 0.5 * {(R-G) + (R-B)} /  root({R-G}^2 + (R-B)(G-B)}) )
    public double getH(double[] value){
        double[] tmpRGB=getLargeRGB(value);

        double H=0.0;
        double tmp=1,num,dec;
        double RG=tmpRGB[0]-tmpRGB[1];
        double RB=tmpRGB[0]-tmpRGB[2];
        double GB=tmpRGB[1]-tmpRGB[2];
        double RG_sq=Math.pow(RG,2);
        num = 0.5 * ((RG) +(RB));

        dec = Math.sqrt(RG_sq  + (RB)*(GB));

        tmp=num/dec;

        H=Math.acos(tmp);
 
        Log.i("HUE T",Double.toString(num));
        Log.i("HUE B",Double.toString(dec));
        Log.i("HUE",Double.toString(tmp));
        return H;
    }
    public double getConcentration(double HueValue){
        double rstHue=HueValue;

        double stage1=3;
        double stage2=2.7;
        double stage3=2.28;
        double stage4=1.46;
        double stage5=1.27;
        double stage6=0.3;

        double[] stage={3,2.7,2.28,1.46,1.27,0.3};
        double diff=10;
        double index=0;
        for(int i=0;i<stage.length;i++){

            if(Math.abs(stage[i]-HueValue)< diff) {
                diff = Math.abs(stage[i] - HueValue);
                index = i+1;
            }
        }
        return index;
    }


//추세선유도 공식 참고
    public HashMap<String,Double> getTrendLine(double[] x,double[] y){
        HashMap<String,Double>ab=new HashMap<>();
        double a=3.7;
        double b=0.5;
        int n=x.length;

        double sum_sq_x=0,sum_y=0,sum_x=0,sum_xy=0;

        for(int i=0;i<n;i++){
            sum_x+= x[i];
            sum_sq_x+= x[i]*x[i];
            sum_y+= y[i];
            sum_xy+= x[i]*y[i];
        }
        a=( n*sum_xy - sum_y*sum_x )/(n*sum_sq_x  -  sum_x * sum_x);
        b= (sum_y-a*sum_x)/n;

        ab.put("a",a);
        ab.put("b",b);

        return ab;
    }





}