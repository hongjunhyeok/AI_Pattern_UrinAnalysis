package com.example.urineanalysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.urineanalysis.utils.CalculateHue;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.urineanalysis.utils.preprocess.ImagePreprocessor;


public class AnalysisActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Analysis_TAG"; // for loggin success or failure messages
    // load camera view of opencv, this let us see using opencv
    private OpenCameraView mOpenCvCameraView;
    private static final int VIEW_MODE_START = 1;
    private static final int VIEW_MODE_INIT = 4;

    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_TIMER_STOP = 102;

    private int mViewMode;
    TextView tx1;
    TextView tx2;
    TextView tx3;
    TextView tx4;
    ProgressBar spinner;


    // used to fix camera orientation from 270 degree to 0 degree
    Mat mRgba, mGray;
    Mat mRgbaF;
    Mat mRgbaT;
    int cropped_x = 0, cropped_y = 0, cropped_w = 1280, cropped_h = 760;

    private CascadeClassifier mJavaDetector;
    private ImagePreprocessor preprocessor;


    // RGB detect

    public int Timer = 1800;
    public int time = 0;
    int count = 0;

    //{{{{do process
    Point p[] = new Point[5];
        Point[] gloucose_p = new Point[6];
    Point[] protein_p = new Point[6];
    Point[] rbc_p = new Point[4];
    Point[] ph_p = new Point[5];

    Point tl=new Point(0,0);
    Point br=new Point(0,0);
    double[] rgbV_GLOUCOSE = new double[3];
    double[] rgbV_PROTEIN = new double[3];
    double[] rgbV_RBC = new double[3];
    double[] rgbV_PH = new double[3];
    double[] rgbV_REFERENCE = new double[3];

    Double tl_x[] = new Double[4];
    Double tl_y[] = new Double[4];

    Double br_x[] = new Double[4];
    Double br_y[] = new Double[4];

    double[] xh =new double[6];
    double[] yh =new double[6];
    double[]xg=new double[6];
    double[]yg=new double[6];
    double[]xp=new double[6];
    double[]yp=new double[6];
    double[] xr =new double[6];
    double[] yr =new double[6];


    double rbc_incline =0.0;
     double ph_incline =0.0;
     double glou_incline=0.0;
    double prot_incline=0.0;

    double graph_rbc_a =0.0;
    double graph_ph_a =0.0;
    double graph_glou_a=0.0;
    double graph_prot_a=0.0;

    double graph_rbc_b =0.0;
    double graph_ph_b =0.0;
    double graph_glou_b=0.0;
    double graph_prot_b=0.0;

    double hue_g=0,hue_p=0,hue_r=0,hue_h=0;
    double idx_g=0,idx_p=0,idx_r=0,idx_h=0;



    double hue_rbc = 0;
    double[] hue_rbcs = new double[4];
    ArrayList<Double> hue_avg = new ArrayList<>();
    ArrayList<String> strR = new ArrayList<>();
    ArrayList<String> strG = new ArrayList<>();
    ArrayList<String> strB = new ArrayList<>();
    ArrayList<String> strH = new ArrayList<>();
    ArrayList<Double> dbH = new ArrayList<>();

//}}}}

    Rect[] bubble_array;
    String msg_rgb;
    String msg_hue="", msg_rbc;
    TimerHandler timerHandler = new TimerHandler();
    Intent intent_to_main;
    CalculateHue calculateHue=null;
    String msgR = "R: ", msgG = "G: ", msgB = "B: ", msgResult = "", msgChart ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preprocessor = new ImagePreprocessor();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_analysis);

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "You need to grant all permission to use this app features", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();

        calculateHue = new CalculateHue();
        Button btn_save = findViewById(R.id.btn_save);
        Button btn_detect = findViewById(R.id.btn_detect);
        Button bt_stop = findViewById(R.id.btn_TimerStop);
        Button bt_up = findViewById(R.id.btn_TimerUp);
        Button bt_dn = findViewById(R.id.btn_TimerDown);

        tx1 = findViewById(R.id.text1);
        tx2 = findViewById(R.id.text2);
        tx3 = findViewById(R.id.text3);
        tx4 = findViewById(R.id.text4);


        //퍼미션 상태 확인
        if (!hasPermissions(PERMISSIONS)) {

            //퍼미션 허가 안되어있다면 사용자에게 요청
            requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        }


        mOpenCvCameraView = (OpenCameraView) findViewById(R.id.activity_surface_view);
//        mOpenCvCameraView.setAF();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1); // front-camera(1),  back-camera(0)
        mOpenCvCameraView.setMaxFrameSize(2000, 2000);


        drawPoint();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap bmp = null;
                Mat subimg = mRgba;
                try {
                    bmp = Bitmap.createBitmap(subimg.cols(), subimg.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(subimg, bmp);
                } catch (CvException e) {
                    Log.d(TAG, e.getMessage());
                }


                subimg.release();
//                mRgba.release();
                FileOutputStream out = null;

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.-HH:mm:ss", Locale.KOREA);
                String filename = "Screenshot_" + sdf.format(date) + ".jpg";



                File sd = new File(Environment.getExternalStorageDirectory().getPath() + "/UrineImages/");



                boolean success = true;
                if (!sd.exists()) {
                    success = sd.mkdir();
                }
                if (success) {
                    File dest = new File(sd, filename);
                    try {
                        out = new FileOutputStream(dest);

                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);


                        // sendBroadCast :: 시스템db에 이미지가 있다는 것을 전달. (나중에 검색하기위해서)
                        getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dest)));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.d(TAG, e.getMessage());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.d(TAG, e.getMessage());

                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                                Log.d(TAG, "OK!!");
                            }
                        } catch (IOException e) {
                            Log.d(TAG, e.getMessage() + "Error");
                            e.printStackTrace();
                        }
                    }
                }


                intent_to_main = new Intent(AnalysisActivity.this, MainActivity.class);
                String tmpH_uro = "";

                intent_to_main.putExtra("hue_urobilinogen", msgResult);

                double[] hue_urobilinogen_for_chart = new double[dbH.size()];

                for (int i = 0; i < dbH.size(); i++) {
                    hue_urobilinogen_for_chart[i] = dbH.get(i);
                }

                intent_to_main.putExtra("chart_urobilinogen", hue_urobilinogen_for_chart);
                Log.i(TAG, "msgResult : " + msgResult);

                startActivity(intent_to_main);
            }
        });

        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewMode = VIEW_MODE_INIT;
                String tmpH_uro = "";


                writeToFileAll();
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                Toast.makeText(getApplicationContext(),"The data saved sucessfully",Toast.LENGTH_SHORT).show();
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
                count=Timer;
                startActivity(i);
            }
        });
        bt_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timer += 5;
                time = Timer;
                Toast.makeText(getApplicationContext(), String.format(Locale.KOREA, "Timer :%d second", Timer), Toast.LENGTH_SHORT).show();
            }
        });
        bt_dn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timer -= 5;
                time = Timer;
                Toast.makeText(getApplicationContext(), String.format(Locale.KOREA, "Timer :%d second", Timer), Toast.LENGTH_SHORT).show();

            }
        });


        btn_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
//                    mViewMode = VIEW_MODE_START;

                    timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);

                    Timer += 5;
                }
                catch (Exception e){
                    Log.i(TAG,e.toString());
                    Toast.makeText(getApplicationContext(),"Focusing 실패 다시눌러누세요",Toast.LENGTH_SHORT).show();
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


        cropped_w = mRgba.width();
        cropped_h = mRgba.height();

        for (int i = 0; i < 4; i++) {


            tl_x[i]=0.0;
            tl_y[i]=0.0;

            br_x[i]=0.0;
            br_y[i]=0.0;
        }
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
        mGray = inputFrame.gray();


//        preprocessor.changeImagePreviewOrientation(mRgba,mRgbaF,mRgbaT);
        final int viewMode = mViewMode;
        switch (viewMode) {
            case VIEW_MODE_START:

                try {
                MatOfRect bubble_rect = new MatOfRect();


                if (mJavaDetector != null) {
                    mJavaDetector.detectMultiScale(mGray, bubble_rect, 2, 1, 0, new Size(), new Size());

                }
                bubble_array = bubble_rect.toArray();


                for (int k = 0; k < bubble_array.length; k++) {


                    if(bubble_array.length>4) break;

                    for (int j=0;j<k;j++){
                        if(Math.abs(bubble_array[j].x-bubble_array[k].x)<50){
                            break;
                        }
                    }

                    tl_x[k] =bubble_array[k].tl().x;
                    tl_y[k] =bubble_array[k].tl().y;
                    br_x[k] =bubble_array[k].br().x;
                    br_y[k] =bubble_array[k].br().y;

//                    Imgproc.rectangle(mRgba, new Point(tl_x[k], tl_y[k]), new Point(br_x[k], br_y[k]), new Scalar(255, 255, 255), 2);
//                    Imgproc.rectangle(mRgba,
//                            new Point(bubble_array[k].tl().x, bubble_array[k].tl().y),
//                            new Point(bubble_array[k].br().x, bubble_array[k].br().y),
//                            new Scalar(255, 255, 255));
                }



                    doProcess();
                } catch (Exception e) {
                    Intent i =new Intent(getApplicationContext(),MainActivity.class);
                    startActivityForResult(i, Activity.RESULT_OK);
                    Log.i(TAG, e.toString());
                }
//                doProcess();
                break;
            case VIEW_MODE_INIT:



                break;

        }


        Imgproc.rectangle(mRgba, new Point(cropped_x, cropped_y), new Point(cropped_x + cropped_w, cropped_y + cropped_h), new Scalar(255, 255, 255), 2);

//        for(int i=0;i<4;i++){
//            Imgproc.rectangle(mRgba, new Point(tl_x[i], tl_y[i]), new Point(br_x[i], br_y[i]), new Scalar(255, 255, 255), 2);
//        }

        return mRgba;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {
                        InputStream is = getResources().openRawResource(R.raw.cascade_circle);


//                        scaleFactor=1.11;minNeighbors=5;
//                        mN1=10; mN2=5; mN3=10;

                        File mCascadeFile, mModelFile2, mModelFile3, mModelFile4;

                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);


                        mCascadeFile = new File(cascadeDir, "cascade_in.xml");


                        FileOutputStream os = new FileOutputStream(mCascadeFile);


//                        FileOutputStream os_mains = new FileOutputStream(mModelFile2);
//                        FileOutputStream os_3=new FileOutputStream(mModelFile3);
                        byte[] buffer = new byte[4096];

                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }


                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mRgba = new Mat();
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
    String[] PERMISSIONS = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions) {

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED) {
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

        switch (requestCode) {

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

        AlertDialog.Builder builder = new AlertDialog.Builder(AnalysisActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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

    // 0 1 2 3 인덱스 = 11, 1, 5, 7시 위치한 좌표
    Double sorted_x[] = new Double[4];
    Double sorted_y[] = new Double[4];


    boolean first = true;

    protected void doProcess() {
        if (bubble_array.length == 4) {


            Double array_x[] = new Double[4];
            Double array_y[] = new Double[4];
            Double maxx = 0.0, maxy = 0.0, minx = 10000.0, miny = 10000.0;

            //각 꼭지점 위치 정보 모음 (순서 상관 없음)
            for (int k = 0; k < bubble_array.length; k++) {
                array_x[k] = (bubble_array[k].tl().x + bubble_array[k].br().x) / 2;
                array_y[k] = (bubble_array[k].tl().y + bubble_array[k].br().y) / 2;
            }

//            Log.i(TAG, "\narray_[0] = {" + String.format(Locale.KOREA, "%.2f , %.2f", array_x[0], array_y[0]) + "}\n" +
//                    "array1 = {" + String.format(Locale.KOREA, "%.2f , %.2f", array_x[1], array_y[1]) + "}\n" +
//                    "array2 = {" + String.format(Locale.KOREA, "%.2f , %.2f", array_x[2], array_y[2]) + "}\n" +
//                    "array3 = {" + String.format(Locale.KOREA, "%.2f , %.2f", array_x[3], array_y[3]) + "}");

            //11,1,5,7시 방향을 0 1 2 3 인덱스로 맞추기 위해 정렬
            double tmp_max = 0, tmp_min = 1000000;
            int index_2 = 0, index_0 = 0;
            for (int k = 0; k < bubble_array.length; k++) {
                if ((array_x[k] + array_y[k]) > tmp_max) {
                    tmp_max = array_x[k] + array_y[k];
                    index_2 = k;
                }


                if ((array_x[k] + array_y[k]) < tmp_min) {
                    tmp_min = array_x[k] + array_y[k];
                    index_0 = k;
                }
            }

            int index_1 = 0, index_3 = 0;
            double tmp_max2 = 0, tmp_min2 = 100000;
            for (int k = 0; k < 4; k++) {
                if (k == index_2 || k == index_0) {
                    continue;
                }

                if (array_x[k] > tmp_max2) {
                    tmp_max2 = array_x[k];
                    index_1 = k;
                }

                if (array_x[k] < tmp_min2) {
                    tmp_min2 = array_x[k];
                    index_3 = k;
                }

            }


            sorted_x[0] = array_x[index_0];
            sorted_y[0] = array_y[index_0];
            sorted_x[1] = array_x[index_1];
            sorted_y[1] = array_y[index_1];
            sorted_x[2] = array_x[index_2];
            sorted_y[2] = array_y[index_2];
            sorted_x[3] = array_x[index_3];
            sorted_y[3] = array_y[index_3];

            String sor = String.format(Locale.KOREA, "%.2f", sorted_x[0]);

//            Log.i(TAG,"\nsorted_x[0],sorted_y[0] = {"+String.format(Locale.KOREA,"%.2f , %.2f",sorted_x[0],sorted_y[0]) +"}\n"+
//                    "sorted_x[1],sorted_y[1] = {"+String.format(Locale.KOREA,"%.2f , %.2f",sorted_x[1],sorted_y[1]) +"}\n"+
//                    "sorted_x[2],sorted_y[2] = {"+String.format(Locale.KOREA,"%.2f , %.2f",sorted_x[2],sorted_y[2]) +"}\n"+
//                    "sorted_x[3],sorted_y[3] = {"+String.format(Locale.KOREA,"%.2f , %.2f",sorted_x[3],sorted_y[3]) +"}");


            double middlex = 0.0, middley = 0.0;
            double[] loc_x = new double[4];
            double[] loc_y = new double[6];

            double distanceX = (sorted_x[1] - sorted_x[0]);

            double distanceY = (sorted_y[1] - sorted_y[0]);
            double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

            double ratio = distance / 290;

            double angle = calculateAngle();

            middlex = (sorted_x[0] + sorted_x[2]) / 2;
            middley = (sorted_y[0] + sorted_y[2]) / 2;


//            for(int i=0;i<4;i++){
//
//                if(i%2==0) {
//                    loc_x[i] = middlex + (distanceX * distanceX_ratio) * i;
//                }
//                Imgproc.circle(mRgba,new Point(loc_x[i],middley),10,new Scalar(255,255,255));
//
//            }
//

            loc_x[0] = middlex - 50;
            loc_x[1] = middlex + 50;
            loc_y[0] = middley - 50;
            loc_y[1] = middley + 50;


            int size = 50;
            for (int i = 0; i <= 4; i++) {
                //angle만큼 회전이동 후 middlex,middley만큼 평행이동.
                //x'=xcos(a)-ysin(a) , y' = xsin(a) + ycos(a)
                double x = middlex + (Math.cos(angle) * p[i].x - Math.sin(angle) * p[i].y) * ratio;
                double y = middley + (Math.sin(angle) * p[i].x + Math.cos(angle) * p[i].y) * ratio;
                double Hue=calculateHue.getH(average_RGB((int)x,(int)y));
                double[] rgb=mRgba.get((int)y,(int)x);
                Imgproc.circle(mRgba, new Point(x, y), (int) (10), new Scalar(255, 255, 255));
//                Imgproc.circle(mRgba, new Point((int)p[i].x,(int)p[i]. y), (int) (10), new Scalar(255, 255, 255));



                if (i == 0) {
                    rgbV_GLOUCOSE = average_RGB((int) x, (int) y);
                    Imgproc.putText(mRgba,String.format(Locale.KOREA,"%.1f" ,calculateHue.getH(rgbV_GLOUCOSE)),new Point(x+10,y),1,2,new Scalar(255,255,255));

                }
                if (i == 1) {
                    rgbV_PROTEIN = average_RGB((int)x, (int)y);
                    Imgproc.putText(mRgba,String.format(Locale.KOREA,"%.1f" ,calculateHue.getH(rgbV_PROTEIN)),new Point(x+10,y),1,2,new Scalar(255,255,255));

                }

                if (i == 2) {
                    rgbV_RBC = average_RGB((int)x, (int)y);
                    Imgproc.putText(mRgba,String.format(Locale.KOREA,"%.1f" ,calculateHue.getH(rgbV_RBC)),new Point(x+10,y),1,2,new Scalar(255,255,255));

                }
                if (i == 3) {
                    rgbV_PH = average_RGB((int) x, (int) y);
                    Imgproc.putText(mRgba,String.format(Locale.KOREA,"%.1f" ,calculateHue.getH(rgbV_PH)),new Point(x+10,y),1,2,new Scalar(255,255,255));

                }
                if (i == 4) {
                    rgbV_REFERENCE = average_RGB((int) x, (int) y);
                    Imgproc.putText(mRgba,String.format(Locale.KOREA,"%.1f" ,calculateHue.getH(rgbV_REFERENCE)),new Point(x+10,y),1,2,new Scalar(255,255,255));
                }
            }

            for (int i=0;i<5;i++){
                xh[i] = middlex + (Math.cos(angle) * ph_p[i].x - Math.sin(angle) * ph_p[i].y) * ratio;
                yh[i] = middley + (Math.sin(angle) * ph_p[i].x + Math.cos(angle) * ph_p[i].y) * ratio;

                Imgproc.circle(mRgba, new Point(xh[i], yh[i]), (int) (10 * ratio), new Scalar(255, 255, 255));
                Imgproc.putText(mRgba,String.format("%.1f",calculateHue.getH(average_RGB((int) xh[i],(int) yh[i]))),new Point(xh[i], yh[i]-25),1,2,new Scalar(255,255,255));
            }
            for (int i=0;i<4;i++){
                xr[i] = middlex + (Math.cos(angle) * rbc_p[i].x - Math.sin(angle) * rbc_p[i].y) * ratio;
                yr[i] = middley + (Math.sin(angle) * rbc_p[i].x + Math.cos(angle) * rbc_p[i].y) * ratio;
                Imgproc.circle(mRgba, new Point(xr[i], yr[i]), (int) (10 * ratio), new Scalar(255, 255, 255));
                Imgproc.putText(mRgba,String.format("%.1f",calculateHue.getH(average_RGB((int) xr[i],(int) yr[i]))),new Point(xr[i]+25, yr[i]),1,2,new Scalar(255,255,255));
            }

            for (int i = 0; i < 6; i++) {
                //angle만큼 회전이동 후 middlex,middley만큼 평행이동.
                //x'=xcos(a)-ysin(a) , y' = xsin(a) + ycos(a)
                xg[i] = middlex + (Math.cos(angle) * gloucose_p[i].x - Math.sin(angle) * gloucose_p[i].y) * ratio;
                yg[i] = middley + (Math.sin(angle) * gloucose_p[i].x + Math.cos(angle) * gloucose_p[i].y) * ratio;
                xp[i] = middlex + (Math.cos(angle) * protein_p[i].x - Math.sin(angle) * protein_p[i].y) * ratio;
                yp[i] = middley + (Math.sin(angle) * protein_p[i].x + Math.cos(angle) * protein_p[i].y) * ratio;

;

                Imgproc.circle(mRgba, new Point(xg[i], yg[i]), (int) (10 * ratio), new Scalar(255, 255, 255));
                Imgproc.circle(mRgba, new Point(xp[i], yp[i]), (int) (10 * ratio), new Scalar(255, 255, 255));
                Imgproc.putText(mRgba,String.format("%.1f",calculateHue.getH(average_RGB((int)xg[i],(int)yg[i]))),new Point(xg[i]+25,yg[i]),1,2,new Scalar(255,255,255));
                Imgproc.putText(mRgba,String.format("%.1f",calculateHue.getH(average_RGB((int)xp[i],(int)yp[i]))),new Point(xp[i],yp[i]-25),1,2,new Scalar(255,255,255));

            }



            Imgproc.putText(mRgba, "Gc", new Point(sorted_x[0], sorted_y[0]), 1, 3, new Scalar(255, 0, 0), 2);
            Imgproc.putText(mRgba, "Pt", new Point(sorted_x[1], sorted_y[1]), 1, 3, new Scalar(255, 0, 0), 2);
            Imgproc.putText(mRgba, "pH", new Point(sorted_x[2], sorted_y[2]), 1, 3, new Scalar(255, 0, 0), 2);
            Imgproc.putText(mRgba, "RBC", new Point(sorted_x[3], sorted_y[3]), 1, 3, new Scalar(255, 0, 0), 2);

            Imgproc.putText(mRgba, String.format(Locale.KOREA, "height : %.2f", 10 * (290 / distance)), new Point(0, 100), 1, 2, new Scalar(255, 0, 0), 2);
            Imgproc.putText(mRgba, String.format(Locale.KOREA, "angle : %.2f", angle), new Point(0, 200), 1, 2, new Scalar(255, 0, 0), 2);
//            Imgproc.putText(mRgba, String.format("hue : %.2f", calculateHue.getH(rgbV_RBC)), new Point(0, 200), 1, 2, new Scalar(255, 0, 0), 2);
//            Imgproc.putText(mRgba, String.format("rgb : %.2f %.2f %.2f",rgbV_RBC[0],rgbV_RBC[1],rgbV_RBC[2]), new Point(0, 300), 1, 2, new Scalar(255, 0, 0), 2);




        }

    }

    double calculateAngle() {
        double dx = sorted_x[1] - sorted_x[0];
        double dy = sorted_y[1] - sorted_y[0];

        double radian = Math.atan(dy / dx);
        double degree = (double) (57.295779 * radian);

        return radian;
    }


    private void writeToFileAll() {

        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss", Locale.KOREA);
        String nowTime = sdf.format(date);


//       for(int i=0;i<strR.size();i++){
//           msgR+=strR.get(i);
//       }
//        for(int i=0;i<strG.size();i++){
//            msgG+=strG.get(i);
//        }
//        for(int i=0;i<strB.size();i++){
//            msgB+=strB.get(i);
//        }
//        for (int i = 0; i < strH.size(); i++) {
//            msgResult += strH.get(i)+"\r\n";
//        }



        msgResult =String.format("%.1f\r\n%.1f\r\n%.1f\r\n%.1f\r\n",20*(idx_g-1),20*(idx_p-1),20*(idx_r-1),20*(idx_h-1));
        msgChart =String.format(Locale.KOREA,"%.1f\r\n%.1f\r\n%.1f\r\n%.1f\r\n",80*(idx_g-1),80*(idx_p-1),250*(idx_r-1),idx_h+3);

        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/OneFileUrineCup");
            File chartfile =new File(Environment.getExternalStorageDirectory() + "/OneFileUrineCup"+"/chart.txt");
            File resultfile =new File(Environment.getExternalStorageDirectory() + "/OneFileUrineCup"+"/result.txt");
            File huefile =new File(Environment.getExternalStorageDirectory() + "/OneFileUrineCup/"+"/hue_"+nowTime+".txt");
            if (!file.exists()) {
                file.mkdir();
            }

            if(!chartfile.exists()){
                chartfile.createNewFile();
            }
            if(!resultfile.exists()){
                resultfile.createNewFile();
            }
            if(!huefile.exists()){
                huefile.createNewFile();
            }
//            FileWriter writer = new FileWriter(file + "/result.txt");
//            writer.append(msgResult);
//            writer.append("\r\n");
//            writer.flush();
//            writer.close();
//            strR.clear();
//            strG.clear();
//            strB.clear();
//            strH.clear();


            //////////////차트용 데이터 생성//////////////////
//            FileReader reader1= new FileReader(file+"/chart.txt");
//            BufferedReader br1=new BufferedReader(reader1);
//            String line;
//            String contents="";
//            while ((line = br1.readLine()) != null) {
//                contents+=(line);
//                contents+="\r\n";
//            }
//            br1.close();
//            reader1.close();
//
//            FileWriter writer1=  new FileWriter(file +"/chart.txt");
//            writer1.append(contents);
//            writer1.append(contents);
//            writer1.append(msgChart);
//            writer1.flush();
//            writer1.close();
            //////////////차트용 데이터 생성//////////////////

            //////////////실험용 데이터 생성//////////////////
//            FileReader reader2= new FileReader(file+"/hue.txt");
//            BufferedReader br2=new BufferedReader(reader2);
//            String line2;
//            String contents2="";
//            while ((line = br2.readLine()) != null) {
//                contents2+=(line);
//                contents2+="\r\n";
//            }
//            br2.close();
//            reader2.close();

            FileWriter writer2 = new FileWriter(huefile);
            writer2.append(sdf.format(date)+"\r\n");
            writer2.append("Time(s) | Glouc | Protein | RBC | PH |\r\n");
            writer2.append(msg_hue);
//            writer2.append(contents2);
            writer2.flush();;
            writer2.close();

            //////////////실험용 데이터 생성//////////////////

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    void drawPoint() {




        int step = 45;
        int loc=35;
        for (int i = 0; i <= 4; i++) {
            if (i == 0) {
                p[0] = new Point(-loc, -loc);
            }
            if (i == 1) {
                p[i] = new Point(loc, -loc);
            }
            if (i == 2) {
                p[i] = new Point(loc, loc);
            }
            if (i == 3) {
                p[i] = new Point(-loc, loc);
            }
            if (i == 4) {
                p[i] = new Point(0, 0);
            }
        }


        for(int i=0;i<6;i++){
            gloucose_p[i]=new Point(-loc,-210+step*i);
            if(i>=3){
                gloucose_p[i]=new Point(-loc,-15+step*i);
            }
        }


        for(int i=0;i<6;i++){
            protein_p[i]=new Point(-210+step*i,-loc);
            if(i>=3){
                protein_p[i]=new Point(-15+step*i,-loc);
            }
        }

        for (int i = 0; i < 4; i++) {
            rbc_p[i] = new Point(loc, -165 + step * i);
            if (i >= 2) {
                rbc_p[i] = new Point(loc, 30 + step * i);
            }

            tl_x[i]=0.0;
            tl_y[i]=0.0;

        }

        for(int i=0;i<5;i++){
            ph_p[i]=new Point(-210 + step*i,loc);
            if(i>2){
                ph_p[i]=new Point(-15 + step*i,loc);
            }
        }



    }

    int countflag = 0;

    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TIMER_START:


                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessage(MESSAGE_TIMER_REPEAT);
                    break;

                case MESSAGE_TIMER_REPEAT:

                    Mat subimg=mRgba;
                    if(!mOpenCvCameraView.isEnabled()) {
                        mOpenCvCameraView.enableView();
                        mOpenCvCameraView.setVisibility(View.VISIBLE);
                    }

                    if (Timer - (count) < 0) {
                        tx1.setText("save Finished");
                        this.sendEmptyMessageDelayed(MESSAGE_TIMER_STOP, 1000);

                    } else {
//                        writeToFile();


                        if(mJavaDetector!=null){


                            MatOfRect bubble_rect=new MatOfRect();
                            mJavaDetector.detectMultiScale(subimg,bubble_rect,2,1);
                            bubble_array=bubble_rect.toArray();

                            for(int i=0;i<bubble_array.length;i++){
                                tl.x = bubble_array[i].tl().x;
                                tl.y = bubble_array[i].tl().y;
                                br.x = bubble_array[i].br().x;
                                br.y = bubble_array[i].br().y;

                                Imgproc.rectangle(mRgba,new Point(tl.x,tl.y),new Point(br.x,br.y),new Scalar(255,255,255),2);
                            }

                            doProcess();
                        }
                        Bitmap bmp=null;


                        try {
                            bmp = Bitmap.createBitmap(subimg.cols(), subimg.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(subimg, bmp);
                        } catch (CvException e) {
                            Log.d(TAG, e.getMessage());
                        }

                        final Bitmap picture =bmp;

//                        mOpenCvCameraView.disableView();
//                        mOpenCvCameraView.setVisibility(View.INVISIBLE);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                ImageView iv = (ImageView) findViewById(R.id.imageView1);
                                iv.setVisibility(View.VISIBLE);

                                iv.setImageBitmap(picture);
                                Log.i(TAG,"RunOnUIThread");

                            }
                        });

                        msg_rgb = "";
                        msg_rbc = "";

                        double hue_G = 0,hue_P=0,hue_R=0,hue_H=0;


                        if(count%5==0) {

                            try {
                                hue_G = calculateHue.getH(rgbV_GLOUCOSE);
                                hue_P = calculateHue.getH(rgbV_PROTEIN);
                                hue_H = calculateHue.getH(rgbV_PH);
                                hue_R = calculateHue.getH(rgbV_RBC);

                                msg_hue += String.format(Locale.KOREA, " %4d %.2f %.2f %.2f %.2f\r\n", count, hue_G, hue_P, hue_R, hue_H);
                                Toast.makeText(getApplicationContext(), String.format(Locale.KOREA, "%d seconds ", (int) (count)), Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        tx1.setText("save remained : " + Integer.toString(count));
                        count += 1;


                    }



                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 3000);


                    ///////////////////////////////////////////////////////////////////////////////반복되는부분///////////////////////////////////////////////////////////////////////
                    try {

                        gloucose_cal();
                        protein_cal();
                        rbc_cal();
                        ph_cal();



                    }catch (Exception e){}


                    break;

                case MESSAGE_TIMER_STOP:
                    Toast.makeText(getApplicationContext(), "Timer Stoped", Toast.LENGTH_SHORT).show();


                    hue_g=calculateHue.getH(rgbV_GLOUCOSE);
                    hue_p=calculateHue.getH(rgbV_PROTEIN);
                    hue_r=calculateHue.getH(rgbV_RBC);
                    hue_h=calculateHue.getH(rgbV_PH);



                    //결과.
                    idx_g=getUrineIndex("gloucose",hue_g);
                    idx_p=getUrineIndex("protein",hue_p);
                    idx_r=getUrineIndex("rbc",hue_r);
                    idx_h=getUrineIndex("ph",hue_h);


                    if(idx_g<1) idx_g=1;
                    if(idx_p<1) idx_p=1;
                    if(idx_r<1) idx_r=1;
                    if(idx_h<1) idx_h=1;
                    Log.i("result:",String.format("%.1f,%.1f,%.1f,%.1f",hue_g,hue_p,hue_r,hue_h));
                    Log.i("result:",String.format("%.1f,%.1f,%.1f,%.1f",idx_g,idx_p,idx_r,idx_h));
                    countflag = 0;
                    msg_rgb = "";
                    msg_rbc = "";
//
                    hue_avg.clear();


                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    break;
            }
        }

    }

    void rbc_cal(){
        double hue_bilirubin = calculateHue.getH(rgbV_RBC);
        double[] y_bilirubin = new double[4];
        double[] x_bilirubin = new double[4];
        for (int i = 0; i < 4; i++) {
            double[] avg = average_RGB((int) xr[i], (int) yr[i]);
            y_bilirubin[i] = calculateHue.getH(avg);
            x_bilirubin[i] = i + 1;

        }

//                    Log.i(TAG,String.format("%.2f %.2f %.2f %.2f //////%.2f %.2f %.2f %.2f",yr[0],yr[1],yr[2],yr[3],y_bilirubin[0],y_bilirubin[1],y_bilirubin[2],y_bilirubin[3]));


        HashMap<String, Double> trendline = null;
        trendline = calculateHue.getTrendLine(x_bilirubin, y_bilirubin);
        Double incline = trendline.get("a");
        graph_rbc_b = trendline.get("b");


           if(!incline.isNaN()){
            rbc_incline += incline;
        }



        msg_rgb = "";

        String msg_Equation;
        msg_rgb = String.format(Locale.KOREA, "R : %.1f\nG: %.1f\nB: %.1f\n", rgbV_RBC[0], rgbV_RBC[1], rgbV_RBC[2]);



        graph_rbc_a = rbc_incline / count;
        String fo=String.format(Locale.KOREA," bili %.2f %.2f %.2f", graph_rbc_a, rbc_incline, incline);
//                        System.out.print("bili:");
//                        System.out.println(graph_rbc_a);
//                        System.out.println(count);

        Log.i(TAG, fo);
        Log.i(TAG, Double.toString(graph_rbc_a));


//                        Log.i(TAG, Double.toString(rbc_incline));
        if (graph_rbc_b < 0) {
            msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_rbc_a)))
                    + "x" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_rbc_b)));
        } else {
            msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_rbc_a)))
                    + "x+" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_rbc_b)));
        }
        tx3.setText(msg_Equation);
    }
    void ph_cal(){
        double hue_ph = calculateHue.getH(rgbV_PH);
        double[] y_ph = new double[4];
        double[] x_ph = new double[4];
        for (int i = 0; i < 4; i++) {
            double[] avg = average_RGB((int) xh[i], (int) yh[i]);
            y_ph[i] = calculateHue.getH(avg);
            x_ph[i] = i + 1;

        }

//                    Log.i(TAG,String.format("%.2f %.2f %.2f %.2f //////%.2f %.2f %.2f %.2f",yr[0],yr[1],yr[2],yr[3],y_bilirubin[0],y_bilirubin[1],y_bilirubin[2],y_bilirubin[3]));


        HashMap<String, Double> trendline = null;
        trendline = calculateHue.getTrendLine(x_ph, y_ph);
        Double incline = trendline.get("a");
        graph_ph_b=trendline.get("b");

        if(!incline.isNaN())
        {
            ph_incline += incline;
        }

        msg_rgb = "";

        String msg_Equation;
        msg_rgb = String.format(Locale.KOREA, "R : %.1f\nG: %.1f\nB: %.1f\n", rgbV_PH[0], rgbV_PH[1], rgbV_PH[2]);



        graph_ph_a =(ph_incline / count);

//                        System.out.print("bili:");
//                        System.out.println(graph_rbc_a);
//                        System.out.println(count);

        Log.i(TAG, String.format("uro %.2f %.2f %.2f", graph_ph_a, ph_incline, incline));
        Log.i(TAG, Double.toString(graph_ph_a));
//                        Log.i(TAG, Double.toString(rbc_incline));
        if (graph_ph_b < 0) {
            msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_ph_a)))
                    + "x" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_ph_b)));
        } else {
            msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_ph_a)))
                    + "x+" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_ph_b)));
        }
        tx4.setText(msg_Equation);
    }

    void gloucose_cal(){
        double hue_gloucose = calculateHue.getH(rgbV_GLOUCOSE);
        double[] y_gloucose = new double[4];
        double[] x_gloucose = new double[4];
        for (int i = 0; i < 4; i++) {
            double[] avg = average_RGB((int) xg[i], (int) yg[i]);
            y_gloucose[i] = calculateHue.getH(avg);
            x_gloucose[i] = i + 1;

        }

//                    Log.i(TAG,String.format("%.2f %.2f %.2f %.2f //////%.2f %.2f %.2f %.2f",yr[0],yr[1],yr[2],yr[3],y_bilirubin[0],y_bilirubin[1],y_bilirubin[2],y_bilirubin[3]));


        HashMap<String, Double> trendline = null;
        trendline = calculateHue.getTrendLine(x_gloucose, y_gloucose);
        Double incline = trendline.get("a");
        graph_glou_b = trendline.get("b");
        if(!incline.isNaN())
        {
            glou_incline += incline;
        }

        msg_rgb = "";

        String msg_Equation;
//        msg_rgb = String.format(Locale.KOREA, "R : %.1f\nG: %.1f\nB: %.1f\n", rgbV_PH[0], rgbV_PH[1], rgbV_PH[2]);
//        msg_hue = String.format(Locale.KOREA, "H: %.2f", hue_urobilinogen);


        graph_glou_a = glou_incline / count;

//                        System.out.print("bili:");
//                        System.out.println(graph_rbc_a);
//                        System.out.println(count);

        Log.i(TAG, String.format("glou %.2f %.2f %.2f", graph_glou_a, glou_incline, incline));
        Log.i(TAG, Double.toString(graph_ph_a));

//                        Log.i(TAG, Double.toString(rbc_incline));
        if (graph_glou_b < 0) {


            msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_glou_a)))
                    + "x" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_glou_b)));
        } else {
            msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_glou_a)))
                    + "x+" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_glou_b)));
        }
        tx1.setText(msg_Equation);
    }
    void protein_cal(){
        double hue_protein = calculateHue.getH(rgbV_PROTEIN);
        double[] y_protein = new double[4];
        double[] x_protein = new double[4];
        for (int i = 0; i < 4; i++) {
            double[] avg = average_RGB((int) xp[i], (int) yp[i]);
            y_protein[i] = calculateHue.getH(avg);
            x_protein[i] = i + 1;

        }

//                    Log.i(TAG,String.format("%.2f %.2f %.2f %.2f //////%.2f %.2f %.2f %.2f",yr[0],yr[1],yr[2],yr[3],y_bilirubin[0],y_bilirubin[1],y_bilirubin[2],y_bilirubin[3]));


        HashMap<String, Double> trendline = null;
        trendline = calculateHue.getTrendLine(x_protein, y_protein);
        Double incline = trendline.get("a");
        graph_prot_b = trendline.get("b");
        if(!incline.isNaN())
        {
            prot_incline += incline;
        }

        msg_rgb = "";

        String msg_Equation;
        msg_rgb = String.format(Locale.KOREA, "R : %.1f\nG: %.1f\nB: %.1f\n", rgbV_PROTEIN[0], rgbV_PROTEIN[1], rgbV_PROTEIN[2]);



        graph_prot_a = prot_incline /(double) count;


//        Log.i(TAG, String.format("%.2f %.2f %.2f", graph_ph_a, ph_incline, incline));
//        Log.i(TAG, Double.toString(graph_ph_a));
//                        Log.i(TAG, Double.toString(rbc_incline));
        if (graph_prot_b < 0) {
            msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_prot_a)))
                    + "x" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_prot_b)));
        } else {
            msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_prot_a)))
                    + "x+" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", graph_prot_b)));
        }
        tx2.setText(msg_Equation);
    }

    double getUrineIndex(String a,double hue){

        double index=0;
        //urine_index= (hue-interept)/incline
        switch (a){
            case "gloucose":
                index=(hue-graph_glou_b)/graph_glou_a;
                break;
            case "protein":
                index=(hue-graph_prot_b)/graph_prot_a;
                break;
            case "rbc":
                index=(hue-graph_rbc_b)/graph_rbc_a;
                break;
            case "ph":
                index=(hue-graph_ph_b)/graph_ph_a;
                break;
            default :
                index=-1;
//                return -1;
        }
        return index;
    }




    double[] average_RGB(int x, int y) {

        double tmpR = 0;
        double tmpG = 0;
        double tmpB = 0;

        double[] avgRGB = new double[3];


        for (int i = x - 1; i < x + 2; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                double[] rgbV = mRgba.get(j, i);

                tmpR += rgbV[0];
                tmpG += rgbV[1];
                tmpB += rgbV[2];
            }
        }

        tmpR = tmpR / 9;
        tmpG = tmpG / 9;
        tmpB = tmpB / 9;

        avgRGB[0] = tmpR;
        avgRGB[1] = tmpG;
        avgRGB[2] = tmpB;

        return avgRGB;

    }

}