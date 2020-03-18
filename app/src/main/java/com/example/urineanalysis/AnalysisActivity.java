package com.example.urineanalysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;
import com.example.urineanalysis.utils.ImageUtils;
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
import java.io.BufferedWriter;
import java.io.File;
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






//https://heartbeat.fritz.ai/working-with-the-opencv-camera-for-android-rotating-orienting-and-scaling-c7006c3e1916





public class AnalysisActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "AnalysisActivity.java"; // for loggin success or failure messages
    // load camera view of opencv, this let us see using opencv
    private OpenCameraView mOpenCvCameraView;
    private static final int VIEW_MODE_START = 1;
    private static final int VIEW_MODE_INIT = 4;

    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_TIMER_STOP = 102;
    private static final int MESSAGE_TIMER_SAVE = 103;

    private int mViewMode;
    TextView tx1;
    TextView tx2;
    TextView tx3;
    TextView tx4;
//    ProgressBar spinner;
    Button bt_cal;

    // used to fix camera orientation from 270 degree to 0 degree
    Mat mRgba, mGray;
    Mat mRgbaF;
    Mat mRgbaT;
    int cropped_x = 0, cropped_y = 0, cropped_w = 1280, cropped_h = 760;
    boolean tmp=false;
    boolean writeFlag=true;

    private CascadeClassifier mJavaDetector;
    private ImagePreprocessor preprocessor;
    private CalculateHue calculateHue;


    // RGB detect

    public int Timer = 10;
    public int time = 0;
    public double hue_value=0.0;




    int count = 0;

    //{{{{do process
    Point middleLine_from[]=new Point[12];
    Point middleLine_to[] = new Point[12];


    Point[] reagents[] = new Point[12][6];

    //    Point[] gloucose_p = new Point[6];
//    Point[] protein_p = new Point[6];
    Point[] bilirubin_p = new Point[4];
//    Point[] urobilinogen_p = new Point[4];


    double[] rgbV_GLOUCOSE = new double[3];
    double[] rgbV_PROTEIN = new double[3];
    double[] rgbV_BILIRUBIN = new double[3];
    double[] rgbV_UROBILINOGEN = new double[3];
    double[] rgbV_1 = new double[3];
    double[] rgbV_2 = new double[3];
    double[] rgbV_3= new double[3];
    double[] rgbV_4 = new double[3];
    double[] rgbV_5 = new double[3];
    double[] rgbV_6 = new double[3];
    double[] rgbV_7= new double[3];
    double[] rgbV_8 = new double[3];
    double[] rgbV_9= new double[3];
    double[] rgbV_10 = new double[3];

    double[] rgbV_REFERENCE = new double[3];
    double[][] rgbV_bilirubins = new double[4][3];


    Double tl_x[] = new Double[4];
    Double tl_y[] = new Double[4];
    Double br_x[] = new Double[4];
    Double br_y[] = new Double[4];



    double hue_1=0,hue_2=0,hue_3=0,hue_4 = 0;


    ArrayList<Double> hue_avg1 = new ArrayList<>();
    ArrayList<Double> hue_avg2 = new ArrayList<>();
    ArrayList<Double> hue_avg3 = new ArrayList<>();
    ArrayList<Double> hue_avg4 = new ArrayList<>();
    ArrayList<Double> hue_avg5 = new ArrayList<>();
    ArrayList<Double> hue_avg6 = new ArrayList<>();
    ArrayList<Double> hue_avg7 = new ArrayList<>();
    ArrayList<Double> hue_avg8 = new ArrayList<>();
    ArrayList<Double> hue_avg9 = new ArrayList<>();
    ArrayList<Double> hue_avg10 = new ArrayList<>();





    ArrayList<Double> dbH1 = new ArrayList<>();
    ArrayList<Double> dbH2 = new ArrayList<>();
    ArrayList<Double> dbH3 = new ArrayList<>();
    ArrayList<Double> dbH4 = new ArrayList<>();
    ArrayList<Double> dbH5 = new ArrayList<>();
    ArrayList<Double> dbH6 = new ArrayList<>();
    ArrayList<Double> dbH7 = new ArrayList<>();
    ArrayList<Double> dbH8 = new ArrayList<>();
    ArrayList<Double> dbH9 = new ArrayList<>();
    ArrayList<Double> dbH10 = new ArrayList<>();


//}}}}

    Rect[] bubble_array;
    Point tl=new Point(0,0);
    Point br=new Point(0,0);



    String msg_rgb;
    String msg_hue, msg_bilirubin;
    TimerHandler timerHandler = new TimerHandler();
    Intent intent_to_main;
    String msgR = "R: ", msgG = "G: ", msgB = "B: ";
    String msgH="";
    String msgH_gloucose="";
    String msgH_protien="";
    String msgH_bilirubin="";
    String msgH_urobilinogen="";

    ImageUtils imageUtils = new ImageUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preprocessor = new ImagePreprocessor();
        calculateHue = new CalculateHue();

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

//        pb=findViewById(R.id.anlysis_progressbar);
        Button btn_back = findViewById(R.id.btn_back);
        Button btn_start = findViewById(R.id.btn_start);
        Button bt_stop = findViewById(R.id.btn_TimerStop);
        Button bt_up = findViewById(R.id.btn_TimerUp);
        Button bt_dn = findViewById(R.id.btn_TimerDown);
        bt_cal= findViewById(R.id.btn_cal);


//        iv.setImageResource(R.drawable.line);
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
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mOpenCvCameraView.setMaxFrameSize(5000, 5000);



//        drawPoint();

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
        });

        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewMode = VIEW_MODE_INIT;
                String tmpH_uro = "";


//                writeToFileAll();
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
            }
        });


        bt_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timer += 100;
                time = Timer;
//                Toast.makeText(getApplicationContext(), String.format(Locale.KOREA, "Timer :%d second", Timer), Toast.LENGTH_SHORT).show();
            }
        });
        bt_dn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timer -= 100;
                time = Timer;
//                Toast.makeText(getApplicationContext(), String.format(Locale.KOREA, "Timer :%d second", Timer), Toast.LENGTH_SHORT).show();

            }
        });


        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//                startActivityForResult(intent, 1111);


                tmp=true;
                writeFlag=true;
                mViewMode = VIEW_MODE_START;


//                pb.setVisibility(View.VISIBLE);
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);

//                Timer += 10;

                CalculateHue calculateHue = new CalculateHue();
                double hue_bilirubin = calculateHue.getH(rgbV_BILIRUBIN);

                double[] y_bilirubin = new double[4];
                double[] x_bilirubin = new double[4];
                for (int i = 0; i < 4; i++) {
                    y_bilirubin[i] = calculateHue.getH(rgbV_BILIRUBIN);
                    x_bilirubin[i] = i + 1;
                }
                HashMap<String, Double> trendline = null;
                trendline = calculateHue.getTrendLine(x_bilirubin, y_bilirubin);


                double incline = trendline.get("a");
                double intercept = trendline.get("b");

                msg_rgb = "";
                msg_hue = "";
                String msg_Equation;

                msg_rgb = String.format(Locale.KOREA, "R : %.1f\nG: %.1f\nB: %.1f\n", rgbV_BILIRUBIN[0], rgbV_BILIRUBIN[1], rgbV_BILIRUBIN[2]);
                msg_hue = String.format(Locale.KOREA, "H: %.2f", hue_bilirubin);


                if (intercept < 0) {
                    msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", incline)))
                            + "x" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", intercept)));
                } else {
                    msg_Equation = "y=" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", incline)))
                            + "x+" + (Double.parseDouble(String.format(Locale.KOREA, "%.2f", intercept)));
                }
                tx2.setText(msg_Equation);


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
        Timer=10;

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
    public void onCameraViewStarted(int height, int width) {
        Mat mat =new Mat(height,width,CvType.CV_8UC1);

        mRgba = new Mat(width, height, CvType.CV_8UC1);
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
        mRgbaT=inputFrame.rgba();



        final int viewMode = mViewMode;
        switch (viewMode) {
            case VIEW_MODE_START:
                MatOfRect bubble_rect = new MatOfRect();

                if (mJavaDetector != null) {
                    mJavaDetector.detectMultiScale(mRgba, bubble_rect, 1.1, 0, 0, new Size(600,400), new Size());

                }
                bubble_array = bubble_rect.toArray();

                if(true) {
                    for (int k = 0; k < bubble_array.length; k++) {
                        tl.x = bubble_array[k].tl().x;
                        tl.y = bubble_array[k].tl().y;
                        br.x = bubble_array[k].br().x;
                        br.y = bubble_array[k].br().y;


                        Rect roi=new Rect(tl,br);

                        Mat mRgbaT=new Mat(mRgba,roi);

                        final Bitmap bm= Bitmap.createBitmap(mRgbaT.cols(), mRgbaT.rows(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mRgbaT,bm);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                ImageView iv = (ImageView) findViewById(R.id.imageView1);
                                iv.setImageBitmap(bm);
                                bt_cal.setEnabled(true);
                            }
                        });


//                        Utils.matToBitmap(mRgbaT,bm);
//                        points=imageUtils.findPoints(bm,500);



//                        if(points.length==4) {
//                            mRgbaT = imageUtils.fourPointTransform(mRgba, points);
//                            final Bitmap outputBitmap = Bitmap.createBitmap(mRgbaT.cols(), mRgbaT.rows(), Bitmap.Config.ARGB_8888);
//
//                            tmp = false;
//                            //쓰레드로 iv 표시(main이 아니기 떄문에 필요
//                            runOnUiThread(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    ImageView iv = (ImageView) findViewById(R.id.imageView1);
//                                    iv.setImageBitmap(outputBitmap);
//                                }
//                            });
//                        }


                        break;
                    }
                }

//                drawRect();
                drawPoint();;



                try {

//                    doProcess();
                } catch (Exception e) {
                    mViewMode = VIEW_MODE_INIT;
                    Log.i("MainActivty", e.toString());
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
                        InputStream is = getResources().openRawResource(R.raw.colorband3);



                        File mCascadeFile, mModelFile2, mModelFile3, mModelFile4;

                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);


                        mCascadeFile = new File(cascadeDir, "cascade_in.xml");


                        FileOutputStream os = new FileOutputStream(mCascadeFile);


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

        for (int i=0;i<10;i++){
            Imgproc.rectangle(mRgba,new Point(30,30*(i+1)+40*(i)), new Point(400,30*(i+1)+40*(i+1)),new Scalar(255,255,255),2);
            Imgproc.rectangle(mRgba,new Point(560,30*(i+1)+40*(i)), new Point(930,30*(i+1)+40*(i+1)),new Scalar(255,255,255),2);
            Imgproc.circle(mRgba, new Point(480, 50+70*(i)), (int) (20), new Scalar(255, 255, 255));

        }



//            Double array_x[] = new Double[4];
//            Double array_y[] = new Double[4];
//            Double maxx = 0.0, maxy = 0.0, minx = 10000.0, miny = 10000.0;
//
//            //각 꼭지점 위치 정보 모음 (순서 상관 없음)
//            for (int k = 0; k < bubble_array.length; k++) {
//                array_x[k] = (bubble_array[k].tl().x + bubble_array[k].br().x) / 2;
//                array_y[k] = (bubble_array[k].tl().y + bubble_array[k].br().y) / 2;
//            }
//
//
//
//            //11,1,5,7시 방향을 0 1 2 3 인덱스로 맞추기 위해 정렬
//            double tmp_max = 0, tmp_min = 1000000;
//            int index_2 = 0, index_0 = 0;
//            for (int k = 0; k < bubble_array.length; k++) {
//                if ((array_x[k] + array_y[k]) > tmp_max) {
//                    tmp_max = array_x[k] + array_y[k];
//                    index_2 = k;
//                }
//
//
//                if ((array_x[k] + array_y[k]) < tmp_min) {
//                    tmp_min = array_x[k] + array_y[k];
//                    index_0 = k;
//                }
//            }
//
//            int index_1 = 0, index_3 = 0;
//            double tmp_max2 = 0, tmp_min2 = 100000;
//            for (int k = 0; k < 4; k++) {
//                if (k == index_2 || k == index_0) {
//                    continue;
//                }
//
//                if (array_x[k] > tmp_max2) {
//                    tmp_max2 = array_x[k];
//                    index_1 = k;
//                }
//
//                if (array_x[k] < tmp_min2) {
//                    tmp_min2 = array_x[k];
//                    index_3 = k;
//                }
//
//            }
//
//
//            sorted_x[0] = array_x[index_0];
//            sorted_y[0] = array_y[index_0];
//            sorted_x[1] = array_x[index_1];
//            sorted_y[1] = array_y[index_1];
//            sorted_x[2] = array_x[index_2];
//            sorted_y[2] = array_y[index_2];
//            sorted_x[3] = array_x[index_3];
//            sorted_y[3] = array_y[index_3];
//
//            String sor = String.format(Locale.KOREA, "%.2f", sorted_x[0]);
//
            double middlex = 0.0, middley = 0.0;
//            double[] loc_x = new double[4];
//            double[] loc_y = new double[6];
//
//            double distanceX = (sorted_x[1] - sorted_x[0]);
//
//            double distanceY = (sorted_y[1] - sorted_y[0]);
//            double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            double distance = 2.0;

            double ratio =1.0;



            middlex = 200;
            middley =200;



            int size = 50;
            double x = middlex;
            double y = middley;


//            rgbV_GLOUCOSE = average_RGB((int) p[2].y, (int) p[2].x);


            for (int i = 0; i <= 4; i++) {
                //angle만큼 회전이동 후 middlex,middley만큼 평행이동.
                //x'=xcos(a)-ysin(a) , y' = xsin(a) + ycos(a)


//                if (i == 0) {
//                    rgbV_GLOUCOSE = average_RGB((int) p[2].y, (int) p[2].x);
//                }
//                if (i == 1) {
//                    rgbV_PROTEIN = average_RGB((int) p[2].y, (int) p[2].x);
//                }
//                if (i == 2) {
//                    rgbV_BILIRUBIN = average_RGB((int) x, (int) y);
//                }
//                if (i == 3) {
//                    rgbV_UROBILINOGEN = average_RGB((int) x, (int) y);
//                }
//                if (i == 4) {
//                    rgbV_REFERENCE = average_RGB((int) x, (int) y);
//                }
            }


//            for (int i = 0; i < 4; i++) {
//                //angle만큼 회전이동 후 middlex,middley만큼 평행이동.
//                //x'=xcos(a)-ysin(a) , y' = xsin(a) + ycos(a)
//                double x = middlex + (Math.cos(angle) * bilirubin_p[i].x - Math.sin(angle) * bilirubin_p[i].y) * ratio;
//                double y = middley + (Math.sin(angle) * bilirubin_p[i].x + Math.cos(angle) * bilirubin_p[i].y) * ratio;
//
//
//                rgbV_bilirubins[i] = average_RGB((int) x, (int) y);
//                Imgproc.circle(mRgba, new Point(x, y), (int) (10 * ratio), new Scalar(255, 255, 255));
//            }


            CalculateHue calculateHue = new CalculateHue();
//            Imgproc.putText(mRgba, "0", new Point(sorted_x[0], sorted_y[0]), 1, 3, new Scalar(255, 0, 0), 2);
//            Imgproc.putText(mRgba, "1", new Point(sorted_x[1], sorted_y[1]), 1, 3, new Scalar(255, 0, 0), 2);
//            Imgproc.putText(mRgba, "2", new Point(sorted_x[2], sorted_y[2]), 1, 3, new Scalar(255, 0, 0), 2);
//            Imgproc.putText(mRgba, "3", new Point(sorted_x[3], sorted_y[3]), 1, 3, new Scalar(255, 0, 0), 2);

//            Imgproc.putText(mRgba, String.format(Locale.KOREA, "height : %.2f", 10 * (290 / distance)), new Point(0, 100), 1, 2, new Scalar(255, 0, 0), 2);
//            Imgproc.putText(mRgba, String.format(Locale.KOREA, "angle : %.2f", angle), new Point(0, 200), 1, 2, new Scalar(255, 0, 0), 2);
//            Imgproc.putText(mRgba, String.format("hue : %.2f", calculateHue.getH(rgbV_BILIRUBIN)), new Point(0, 200), 1, 2, new Scalar(255, 0, 0), 2);
//            Imgproc.putText(mRgba, String.format("rgb : %.2f %.2f %.2f",rgbV_BILIRUBIN[0],rgbV_BILIRUBIN[1],rgbV_BILIRUBIN[2]), new Point(0, 300), 1, 2, new Scalar(255, 0, 0), 2);

            hue_1=calculateHue.getH(rgbV_GLOUCOSE);
            hue_2=calculateHue.getH(rgbV_PROTEIN);
            hue_3 = calculateHue.getH(rgbV_BILIRUBIN);
            hue_4=calculateHue.getH(rgbV_UROBILINOGEN);



            hue_avg1.add(hue_1);
            hue_avg2.add(hue_2);
            hue_avg3.add(hue_3);
            hue_avg4.add(hue_4);
            if (hue_avg1.size() > 1000 ) {
                hue_avg1.clear();
                hue_avg2.clear();
                hue_avg3.clear();
                hue_avg4.clear();
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

        int index=1;
        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd", Locale.KOREA);
        String nowTime = sdf.format(date);



        double tmp=0;

        //시간, 날짜 구분하기 위한 `-` 추가.
        try {

            for (int i = 0; i < dbH1.size(); i++) {
                tmp+= dbH1.get(i);
            }
            msgH_gloucose= String.format(Locale.KOREA,"-%.3f",tmp/dbH1.size());
            tmp=0;

            for (int i = 0; i < dbH2.size(); i++) {
                tmp += dbH2.get(i);
            }
            msgH_protien= String.format(Locale.KOREA,"-%.3f",tmp/dbH2.size());
            tmp=0;

            for (int i = 0; i < dbH3.size(); i++) {
                tmp += dbH3.get(i);
            }
            msgH_bilirubin= String.format(Locale.KOREA,"-%.3f",tmp/dbH3.size());
            tmp=0;

            for (int i = 0; i < dbH4.size(); i++) {
                tmp += dbH4.get(i);
            }
            msgH_urobilinogen = String.format(Locale.KOREA,"-%.3f",tmp/dbH4.size());

            tmp = 0;

        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }

        String[] sssss=nowTime.split("-");

        try {


            File UrineDir = new File(Environment.getExternalStorageDirectory() + "/OneFileUrineCup");

            File txt1=new File(UrineDir+"/gloucose.txt");
            File txt2=new File(UrineDir+"/protein.txt");
            File txt3=new File(UrineDir+"/bilirubin.txt");
            File txt4=new File(UrineDir+"/urobilinogen.txt");

            if (!UrineDir.exists()) {
                UrineDir.mkdir();
            }

            if (!UrineDir.exists() || !txt4.exists() || !txt1.exists() || !txt2.exists() || !txt3.exists())  {
                UrineDir.createNewFile();
                txt1.createNewFile();
                txt2.createNewFile();
                txt3.createNewFile();
                txt4.createNewFile();
            }

            FileReader reader1 =new FileReader(UrineDir+"/gloucose.txt");
//            FileReader reader2 =new FileReader(UrineDir+"/protein.txt");
//            FileReader reader3 =new FileReader(UrineDir+"/bilirubin.txt");
//            FileReader reader4 =new FileReader(UrineDir+"/urobilinogen.txt");

            BufferedReader rd1=new BufferedReader(reader1);
//            BufferedReader rd2=new BufferedReader(reader2);
//            BufferedReader rd3=new BufferedReader(reader4);
//            BufferedReader rd4=new BufferedReader(reader4);


            while(rd1.readLine() != null){
                index++;
            }
            String ex= index+"-"+sssss[0]+"."+sssss[1]+"."+sssss[2];


            //filewriter = 파일 쓰기 도구. BufferWriter = 이어쓰기 위해 사용한 도구
            FileWriter writer1 = new FileWriter(UrineDir +"/gloucose.txt",true);
            FileWriter writer2 = new FileWriter(UrineDir +"/protein.txt",true);
            FileWriter writer3 = new FileWriter(UrineDir +"/bilirubin.txt",true);
            FileWriter writer4 = new FileWriter(UrineDir +"/urobilinogen.txt",true);

            BufferedWriter bw1 = new BufferedWriter(writer1);
            BufferedWriter bw2 = new BufferedWriter(writer2);
            BufferedWriter bw3 = new BufferedWriter(writer3);
            BufferedWriter bw4 = new BufferedWriter(writer4);


            bw1.append(ex+msgH_gloucose);
            bw1.append("\r\n");

            bw2.append(ex+msgH_protien);
            bw2.append("\r\n");

            bw3.append(ex+msgH_bilirubin);
            bw3.append("\r\n");

            bw4.append(ex+msgH_urobilinogen);
            bw4.append("\r\n");

            bw1.flush();
            bw1.close();

            bw2.flush();
            bw2.close();

            bw3.flush();
            bw3.close();

            bw4.flush();
            bw4.close();




            dbH1.clear();
            dbH2.clear();
            dbH3.clear();
            dbH4.clear();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //도형 그림.
    void drawRect(){
        Imgproc.rectangle(mRgba,tl,br,new Scalar(255,255,255),5);


        int center_x=(int)((br.x+tl.x)/2);
        int center_y=(int)((br.y+tl.y)/2);
        int width=(int)(br.x-tl.x);
        int height=(int)(br.y-tl.y);
        int len=(int)(width * 0.038);



        // Tester 용지가 들어올 부분
        middleLine_from[0]=new Point(tl.x+50,center_y-(int)(len/2));
        middleLine_to[0]=new Point(middleLine_from[0].x+len,center_y+(int)(len/2));



        // Tester 용지 각 패드 위치 설정
        for (int i=1;i<12;i++){
            middleLine_from[i] = new Point(middleLine_to[i-1].x  + len, center_y-(int)(len/2));
            middleLine_to[i] = new Point(middleLine_from[i].x + len,center_y+(int)(len/2) );
        }

        // 각 패드마다 비색표 위치 설정
        for (int i=0;i<12;i++){

            for (int j=0;j<6;j++) {
                if (j == 0) {
                    reagents[i][j] = new Point((middleLine_from[i].x + middleLine_to[i].x) / 2, br.y - 100);
                }

                if (j == 1 || j == 2) {
                    reagents[i][j] = new Point((middleLine_from[i].x + middleLine_to[i].x) / 2, reagents[i][j - 1].y - 2 * len);
                }

                if (j == 3){
                    reagents[i][j]=new Point((middleLine_from[i].x+middleLine_to[i].x)/2,middleLine_from[i].y-100);
                }

                if (j == 4 || j == 5) {
                    reagents[i][j] = new Point((middleLine_from[i].x + middleLine_to[i].x) / 2, reagents[i][j - 1].y - 2 * len);
                }
            }
        }


        // roi 그리고 검사실시
               for (int i=0;i<12;i++){


            Imgproc.rectangle(mRgba, middleLine_from[i], middleLine_to[i],new Scalar(255,255,255),5);


            if (i==0 || i==2 || i==4 || i==7 || i==8){
                for (int j=0;j<6;j++){
                    Imgproc.circle(mRgba, reagents[i][j], len/2, new Scalar(255, 255, 255), 3);

                    if(writeFlag) {
                        rgbV_1 = mRgba.get((int) reagents[i][j].y, (int) reagents[i][j].x);
                        hue_value = calculateHue.getH(rgbV_1);

                        Log.i(TAG, String.format("reagent i j %d %d, Hue : %.1f, RGB : %.1f %.1f %.1f", i, j, hue_value, rgbV_1[0], rgbV_1[1], rgbV_1[2]));
                    }

                }
            }
            if (i==3 || i==6 || i==9){
                for (int j=0;j<5;j++){
                    Imgproc.circle(mRgba, reagents[i][j], len/2, new Scalar(255, 255, 255), 3);


                    if(writeFlag) {
                        rgbV_1 = mRgba.get((int) reagents[i][j].y, (int) reagents[i][j].x);


                        hue_value = calculateHue.getH(rgbV_1);
                        Log.i(TAG, String.format("reagent i j %d %d, Hue : %.1f, RGB : %.1f %.1f %.1f", i, j, hue_value, rgbV_1[0], rgbV_1[1], rgbV_1[2]));
                    }
                }
            }
            if (i==1 || i==10){
                for (int j=0;j<4;j++){
                    Imgproc.circle(mRgba, reagents[i][j], len/2, new Scalar(255, 255, 255), 3);

                    if(writeFlag) {

                        rgbV_1 = mRgba.get((int) reagents[i][j].y, (int) reagents[i][j].x);
                        hue_value = calculateHue.getH(rgbV_1);

                        Log.i(TAG, String.format("reagent i j %d %d, Hue : %.1f, RGB : %.1f %.1f %.1f", i, j, hue_value, rgbV_1[0], rgbV_1[1], rgbV_1[2]));
                    }
                }
            }
            if (i==5){
                for (int j=0;j<5;j+=3){
                    Imgproc.circle(mRgba, reagents[i][j], len/2, new Scalar(255, 255, 255), 3);

                    if(writeFlag) {
                        rgbV_1 = mRgba.get((int) reagents[i][j].y, (int) reagents[i][j].x);
                        hue_value = calculateHue.getH(rgbV_1);

                        Log.i(TAG, String.format("reagent i j %d %d, Hue : %.1f, RGB : %.1f %.1f %.1f", i, j, hue_value, rgbV_1[0], rgbV_1[1], rgbV_1[2]));
                    }
                }
            }
            if (i==11){

                Imgproc.circle(mRgba, reagents[i][0], len/2, new Scalar(255, 255, 255), 3);

                if(writeFlag) {


                    rgbV_1 = mRgba.get((int) reagents[i][0].y, (int) reagents[i][0].x);
                    hue_value = calculateHue.getH(rgbV_1);
                    Log.i(TAG, String.format("reagent i j %d %d, Hue : %.1f, RGB : %.1f %.1f %.1f", i, 0, hue_value, rgbV_1[0], rgbV_1[1], rgbV_1[2]));
                    writeFlag=false;
                }

            }


        }




    }
    void drawPoint() {

    Imgproc.rectangle(mRgba,new Point(50,50),new Point(850,500),new Scalar(255,255,255));

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

                    if (count > Timer) {
//                        tx1.setText("save Finished");
                        this.sendEmptyMessageDelayed(MESSAGE_TIMER_STOP, 1000);

                    } else {
//                        writeToFile();

                        msg_rgb = "";
                        msg_hue = "";
                        msg_bilirubin = "";

                        Double avg_hueValue1 = 0.0,avg_hueValue2=0.0,avg_hueValue3=0.0,avg_hueValue4=0.0;


                        //잡음제거를 위함.
                        for (int i = 0; i < hue_avg1.size(); i++) {
                            avg_hueValue1 += hue_avg1.get(i);
                        }
                        avg_hueValue1 = avg_hueValue1 / hue_avg1.size();

                        for (int i = 0; i < hue_avg2.size(); i++) {
                            avg_hueValue2 += hue_avg2.get(i);
                        }
                        avg_hueValue2 = avg_hueValue2 / hue_avg2.size();

                        for (int i = 0; i < hue_avg3.size(); i++) {
                            avg_hueValue3 += hue_avg3.get(i);
                        }
                        avg_hueValue3 = avg_hueValue3 / hue_avg3.size();

                        for (int i = 0; i < hue_avg4.size(); i++) {
                            avg_hueValue4 += hue_avg4.get(i);
                        }
                        avg_hueValue4 = avg_hueValue4 / hue_avg4.size();

                        hue_avg1.clear();
                        hue_avg2.clear();
                        hue_avg3.clear();
                        hue_avg4.clear();


                        if(!avg_hueValue1.isNaN() && !avg_hueValue2.isNaN() && !avg_hueValue3.isNaN() && !avg_hueValue4.isNaN())
                        {
                            dbH1.add(avg_hueValue1);
                            dbH2.add(avg_hueValue2);
                            dbH3.add(avg_hueValue3);
                            dbH4.add(avg_hueValue4);
                        }




                        Log.i(TAG,String.format("%.2f %.2f %.2f %.2f",avg_hueValue1,avg_hueValue2,avg_hueValue3,avg_hueValue4));
//                        tx1.setText("save remained : " + Integer.toString((Timer - count) / 10));
                        count += 10;
                    }


                       if (countflag == 0) {
                            countflag++;

                        this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 500);

                    } else {
                        this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 1000);
                    }

                    break;

                case MESSAGE_TIMER_STOP:
//                    Toast.makeText(getApplicationContext(), "Timer Stoped", Toast.LENGTH_SHORT).show();

                    countflag = 0;
                    msg_rgb = "";
                    msg_hue = "";
                    msg_bilirubin = "";
                    hue_avg3.clear();


                    writeToFileAll();


                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_SAVE, 500);

                    this.removeMessages(MESSAGE_TIMER_REPEAT);



                    break;
//                case MESSAGE_TIMER_SAVE:
//                    Bitmap bmp = null;
//                    Mat subimg = mRgba;
//                    try {
//                        bmp = Bitmap.createBitmap(subimg.cols(), subimg.rows(), Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(subimg, bmp);
//                    } catch (CvException e) {
//                        Log.d(TAG, e.getMessage());
//                    }
//
//
//                    subimg.release();
////                mRgba.release();
//                    FileOutputStream out = null;
//
//                    long now = System.currentTimeMillis();
//                    Date date = new Date(now);
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.KOREA);
//                    String filename = "1.jpg";
//
//
//                    File sd = new File(Environment.getExternalStorageDirectory().getPath() + "/UrineImages/");
//
//
//                    boolean success = true;
//                    if (!sd.exists()) {
//                        success = sd.mkdir();
//                    }
//                    if (success) {
//                        File dest = new File(sd, filename);
//                        try {
//                            if(dest.exists())
//                                dest.delete();
//                            out = new FileOutputStream(dest);
//                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
//
//                            // sendBroadCast :: 시스템db에 이미지가 있다는 것을 전달. (나중에 검색하기위해서)
//                            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dest)));
//                        } catch (NullPointerException e) {
//                            e.printStackTrace();
//                            Log.d(TAG, e.getMessage());
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                            Log.d(TAG, e.getMessage());
//
//                        } finally {
//                            try {
//                                if (out != null) {
//                                    out.close();
//                                    Log.d(TAG, "OK!!");
//                                }
//                            } catch (IOException e) {
//                                Log.d(TAG, e.getMessage() + "Error");
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    pb.setVisibility(View.GONE);
//                    mViewMode=VIEW_MODE_INIT;
//                    onResume();
//                    Intent i = new Intent(getApplicationContext(),ResultActivity.class);
//                    startActivity(i);
            }
        }

    }

    double[] average_RGB(int x, int y) {

        double tmpR = 0;
        double tmpG = 0;
        double tmpB = 0;

        double[] avgRGB = new double[3];


        for (int i = x - 2; i < x + 3; i++) {
            for (int j = y - 2; j < y + 3; j++) {
                double[] rgbV = mRgba.get(j, i);

                tmpR += rgbV[0];
                tmpG += rgbV[1];
                tmpB += rgbV[2];
            }
        }

        tmpR = tmpR / 25;
        tmpG = tmpG / 25;
        tmpB = tmpB / 25;

        avgRGB[0] = tmpR;
        avgRGB[1] = tmpG;
        avgRGB[2] = tmpB;

        return avgRGB;

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //https://black-jin0427.tistory.com/120 참고했음.



        //분석뒤 -> 앨범으로 자동이동

        if(requestCode==1111) {
            Cursor cursor = null;
            File tempFile=null;
            try {
                Uri photoUri = data.getData();

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = {MediaStore.Images.Media.DATA};
                assert photoUri != null;    //assert :: true -> pass false-> stop & message
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }



        }


    }

    private void setImages() {

    }
}