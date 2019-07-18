package com.example.urineanalysis;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "opencv_TAG"; // for loggin success or failure messages
    // load camera view of opencv, this let us see using opencv
    private CameraBridgeViewBase mOpenCvCameraView;


    // used in camera selection from menu
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    // used to fix camera orientation from 270 degree to 0 degree
    Mat mRgba,mGray,cropped_img;
    Mat mRgbaF;
    Mat mRgbaT;
    int cropped_x=0,cropped_y=0,cropped_w=1280,cropped_h=760;
    long mRgba_Hue;

    private File mCascadeFile, mModelFile2, mModelFile3,mModelFile4;
    private CascadeClassifier	mJavaDetector,mJavaDetector2,mJavaDetector3,mJavaDetector4;


    // RGB detect
    double RGB[][] = new double[4][3];
    double RGB1[]=new double[3];
    double RGB2[]=new double[3];


    double RGB3[]=new double[3];
    double RGB4[]=new double[3];
    // HSV detect
    double HSV[][]=new double[4][3];
    double HSV_[][]=new double[4][3]; // 정규화된 HSV

//    private CascadeClassifier mJavaDetector2;
//    private CascadeClassifier mJavaDetector3;
//    private CascadeClassifier mJavaDetector4;

    int box4_circle_center_x=100;
    int box4_circle_center_y=100;
    int box11_circle_center_x=200;
    int box11_circle_center_y=200;
    int box18_circle_center_x=300;
    int box18_circle_center_y=300;
    int box25_circle_center_x=400;
    int box25_circle_center_y=400;
    double[] RGB_value=new double[3];
    double[] RGB_value2=new double[3];
    double[] RGB_value3=new double[3];
    double[] RGB_value4=new double[3];

    double[][] RGB_value_GLOUCOSE = new double[6][3];

    double calcul_H[][]=new double[4][3]; //  4개의 영역에 대한 H 값 -> 깃허브수식사용
    Rect[] bubble_array;
    Rect[] array2;
    Rect[] array3;
    Rect[] array4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        Button bt=(Button)findViewById(R.id.button1);
        final TextView tx1=(TextView)findViewById(R.id.text1);
        final TextView tx2=(TextView)findViewById(R.id.text2);
        final TextView tx3=(TextView)findViewById(R.id.text3);
        final TextView tx4=(TextView)findViewById(R.id.text4);

        String result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mOpenCvCameraView.setMaxFrameSize(2000,2000);



        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String text1 = "R: " + Double.toString(RGB_value[0]) + "G: " + Double.toString(RGB_value[1]) + "B: " + Double.toString(RGB_value[2]);
                    tx1.setText(text1);

                    String text2 = "R: " + Double.toString(RGB_value2[0]) + "G: " + Double.toString(RGB_value2[1]) + "B: " + Double.toString(RGB_value2[2]);
                    tx2.setText(text2);

                    String text3 = "R: " + Double.toString(RGB_value3[0]) + "G: " + Double.toString(RGB_value3[1]) + "B: " + Double.toString(RGB_value3[2]);
                    tx3.setText(text3);

                    String text4 = "R: " + Double.toString(RGB_value4[0]) + "G: " + Double.toString(RGB_value4[1]) + "B: " + Double.toString(RGB_value4[2]);
                    tx4.setText(text4);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplication(),e.toString(),Toast.LENGTH_SHORT).show();
                }

                CalculateHue calculateHue=new CalculateHue();
                double hue=calculateHue.getH(RGB_value);

                double[] y_gloucose = new double[6];
                double[] x_gloucose = new double[6];
                for(int i=0;i<6;i++){
                    y_gloucose[i]=calculateHue.getH(RGB_value_GLOUCOSE[i]);
                    x_gloucose[i]=i+1;
                }
                HashMap<String,Double>trendline=null;

                trendline=calculateHue.getTrendLine(x_gloucose,y_gloucose);

                double incline=  trendline.get("a");
                double intercept= trendline.get("b");

                double rst= calculateHue.getConcentration(hue);
                Intent i = new Intent(getApplicationContext(),Chart.class);
                i.putExtra("hue",hue);
                i.putExtra("index",rst);
                tx1.append("rst : "+Double.toString(rst));
                startActivity(i);

                String msg;
                if(intercept<0) {
                     msg = "y=" + Double.toString(Double.parseDouble(String.format("%.2f", incline)))
                            + "x"+Double.toString(Double.parseDouble(String.format("%.2f",intercept)));
                }else{
                    msg = "y=" + Double.toString(Double.parseDouble(String.format("%.2f", incline)))
                            + "x+"+Double.toString(Double.parseDouble(String.format("%.2f",intercept)));
                }
                tx2.setText(msg);
                for(int j=0;j<6;j++){
                    Log.i(TAG,Double.toString(Double.parseDouble(String.format("%.2f",y_gloucose[j]))));
                }
            }
        });

    }
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC3);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);

    }


    // destroy image data when you stop camera preview
    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        // TODO Auto-genetated method stub
        mRgba = inputFrame.rgba();
        mGray=inputFrame.gray();

        MatOfRect bubble_rect=new MatOfRect();
        MatOfRect rect2=new MatOfRect();
        MatOfRect rect3=new MatOfRect();
        MatOfRect rect4=new MatOfRect();


//        Point t1= new Point(cropped_x,cropped_y);
//        Point t2 = new Point(cropped_x+cropped_w,cropped_y+cropped_h);
//        cropped_img = new Mat(mGray,new Rect(t1,t2));

        if(mJavaDetector!=null){
            mJavaDetector.detectMultiScale(mGray,bubble_rect,1.3,5,0,new Size(),new Size());
            mJavaDetector2.detectMultiScale(mGray,rect2,1.5,5,0,new Size(),new Size());
            mJavaDetector3.detectMultiScale(mGray,rect2,1.5,5,0,new Size(),new Size());
            mJavaDetector4.detectMultiScale(mGray,rect2,1.5,5,0,new Size(),new Size());
        }
        bubble_array= bubble_rect.toArray();
        array2=rect2.toArray();
        array3=rect3.toArray();
        array4=rect4.toArray();



        double corner1_circle_center_x=0;
        double corner1_circle_center_y=0;
        for(int k=0;k<bubble_array.length;k++){
            Imgproc.rectangle(mRgba,
                    new Point(bubble_array[k].tl().x,bubble_array[k].tl().y),
                    new Point(bubble_array[k].br().x,bubble_array[k].br().y),
                    new Scalar(255,0,0));

        }

        for(int k=0;k<array2.length;k++){
            Imgproc.rectangle(mRgba,
                    new Point(array2[k].tl().x,array2[k].tl().y),
                    new Point(array2[k].br().x,array2[k].br().y),
                    new Scalar(255,255,255));
        }
        for(int k=0;k<array3.length;k++){
            Imgproc.rectangle(mRgba,
                    new Point(array3[k].tl().x,array3[k].tl().y),
                    new Point(array3[k].br().x,array3[k].br().y),
                    new Scalar(0,0,0));
        }
        for(int k=0;k<array4.length;k++){
            Imgproc.rectangle(mRgba,
                    new Point(array4[k].tl().x,array4[k].tl().y),
                    new Point(array4[k].br().x,array4[k].br().y),
                    new Scalar(100,0,255));
        }
        Imgproc.rectangle(mRgba,new Point(cropped_x,cropped_y),new Point(cropped_x+cropped_w,cropped_y+cropped_h),new Scalar(255,255,255),2);
        return mRgba;



    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {
                        InputStream is = getResources().openRawResource(R.raw.cascade00);
                        InputStream is_man=getResources().openRawResource(R.raw.cascade_oct);
                        InputStream is_3=getResources().openRawResource(R.raw.cascade_b3);
                        InputStream is_4=getResources().openRawResource(R.raw.cascade_b4);

//                        scaleFactor=1.11;minNeighbors=5;
//                        mN1=10; mN2=5; mN3=10;


                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File manisDir=getDir("cascade",Context.MODE_PRIVATE);
                        File model3Dir=getDir("cascade",Context.MODE_PRIVATE);
                        File model4Dir=getDir("cascade",Context.MODE_PRIVATE);

                        mCascadeFile = new File(cascadeDir, "cascade_in.xml");
                        mModelFile2 = new File(manisDir,"cascade_out4.xml");
                        mModelFile3= new File(model3Dir,"cascade_empty_6.xml");
                        mModelFile4= new File(model3Dir,"cascade_empty_7.xml");

                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        FileOutputStream os_mains = new FileOutputStream(mModelFile2);
                        FileOutputStream os_3=new FileOutputStream(mModelFile3);
                        FileOutputStream os_4=new FileOutputStream(mModelFile4);

//                        FileOutputStream os_mains = new FileOutputStream(mModelFile2);
//                        FileOutputStream os_3=new FileOutputStream(mModelFile3);
                        byte[] buffer = new byte[4096];
                        byte[] buffer2 = new byte[4096];
                        byte[] buf3 = new byte[4096];

                        byte[] buf4 = new byte[4096];
                        int bytesRead,bytesRead2,bytesRead3,bytesRead4;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        while ((bytesRead2 = is_man.read(buffer2)) != -1) {
                            os_mains.write(buffer2, 0, bytesRead2);
                        }
                        while((bytesRead3 = is_3.read(buf3))!=-1){
                            os_3.write(buf3,0,bytesRead3);
                        }
                        while((bytesRead4 = is_4.read(buf4))!=-1){
                            os_4.write(buf4,0,bytesRead4);
                        }

                        is.close();
                        is_man.close();
                        is_3.close();
                        is_4.close();
                        os.close();
                        os_mains.close();
                        os_3.close();
                        os_4.close();
                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        mJavaDetector2 =new CascadeClassifier(mModelFile2.getAbsolutePath());
                        mJavaDetector3= new CascadeClassifier(mModelFile3.getAbsolutePath());
                        mJavaDetector4 =new CascadeClassifier(mModelFile4.getAbsolutePath());

                        cascadeDir.delete();manisDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mRgba=new Mat();
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    /////////////////////////////////여기서부턴 퍼미션 관련 메소드///////////////////////////////////////
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted)
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }

    protected void doProcess(){
        if(bubble_array.length==1 && array2.length==1 && array3.length==1 && array4.length==1) {


            Double array_x[]=new Double[4];
            Double array_y[]=new Double[4];
            Double maxx=0.0,maxy=0.0,minx=10000.0,miny=10000.0;

            for (int k = 0; k < bubble_array.length; k++) {
                Log.i(TAG,k+1+"번째 위치"+bubble_array[k].tl().x+","+bubble_array[k].tl().y);
                array_x[k]=(bubble_array[k].tl().x+bubble_array[k].br().x)/2;
                array_y[k]=(bubble_array[k].tl().y+bubble_array[k].br().y)/2;
            }
            for (int i=0;i<4;i++){
                if(maxx < array_x[i])maxx=array_x[i];
                if(maxy < array_y[i])maxy=array_y[i];
                if(minx > array_x[i])minx=array_x[i];
                if(miny > array_y[i])miny=array_y[i];
            }
            double middlex=0.0,middley=0.0;
            double[] loc_y=new double[6];
            double[][] rgbV11= new double[6][3];
            middlex=(maxx+minx)/2;
            middley=(maxy+miny)/2;

            for(int i=0;i<6;i++){
                if(i>=3)
                    loc_y[i]=middley+(50*i);
                else
                    loc_y[i]=middley-250+(50*i);
                Imgproc.circle(mRgba,new Point(middlex,loc_y[i]),15,new Scalar(255,255,255));
                rgbV11[i]=mRgba.get((int)loc_y[i],(int)middlex);
            }

            Imgproc.circle(mRgba,new Point(middlex,middley),10,new Scalar(255,255,255));
            Imgproc.circle(mRgba,new Point(middlex+73,middley),10,new Scalar(255,255,255));
            Imgproc.circle(mRgba,new Point(middlex+146,middley),10,new Scalar(255,255,255));
            Imgproc.circle(mRgba,new Point(middlex+219,middley),10,new Scalar(255,255,255));

            double[] rgbV1 = mRgba.get((int)middley, (int)middlex);
            double[] rgbV2 = mRgba.get((int)(middley), (int)middlex+73);
            double[] rgbV3 = mRgba.get((int)(middley), (int)middlex+146);
            double[] rgbV4 = mRgba.get((int)(middley), (int)middlex+219);

            RGB_value= rgbV1;
            RGB_value2=rgbV2;
            RGB_value3=rgbV3;
            RGB_value4=rgbV4;
            RGB_value_GLOUCOSE=rgbV11;
        }

    }

}
