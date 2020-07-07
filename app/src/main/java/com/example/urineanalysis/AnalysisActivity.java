package com.example.urineanalysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import java.io.BufferedWriter;
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

    Button btn_detect;

    // used to fix camera orientation from 270 degree to 0 degree
    Mat mRgba, mGray;
    Mat mRgbaF;
    Mat mRgbaT;
    int cropped_x = 0, cropped_y = 0, cropped_w = 1280, cropped_h = 760;
    boolean tmp=false;
    boolean writeFlag=true;

    private CascadeClassifier mJavaDetector;
    private CalculateHue calculateHue;


    // RGB detect

    public int Timer = 10;


    int count = 0;

    //{{{{do process


    Point[] bilirubin_p = new Point[4];

    int frame_num=0;


;
    double[][] colorbandHue = new double[11][4];
    double[] stripHue = new double[11];
    double[] resultIndex= new double[11];


    double[] rgbV_REFERENCE = new double[3];
    double[][] rgbV_bilirubins = new double[4][3];


    Double tl_x[] = new Double[4];
    Double tl_y[] = new Double[4];
    Double br_x[] = new Double[4];
    Double br_y[] = new Double[4];




//}}}}

    Rect[] bubble_array;
    Point tl=new Point(0,0);
    Point br=new Point(0,0);


    boolean is_detected=false;
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

        calculateHue = new CalculateHue();


        ///////전체타이틀
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_analysis);
        //전체타이틀/////////////

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
        btn_detect = findViewById(R.id.btn_cal);


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


        btn_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!is_detected) {
                    try {
                        mViewMode = VIEW_MODE_START;

                        timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);

                        Timer += 5;

                        is_detected = !is_detected;
                    } catch (Exception e) {
                        Log.i(TAG, e.toString());
                        Toast.makeText(getApplicationContext(), "Focusing 실패 다시눌러주세요", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    try {
                        mViewMode = VIEW_MODE_INIT;
                        is_detected = !is_detected;


//
                        btn_detect.setText("DETECT");

                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        Toast.makeText(getApplicationContext(), "The data saved sucessfully", Toast.LENGTH_SHORT).show();
                        timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
                        count = Timer;
                        startActivity(i);

                        Toast.makeText(getApplicationContext(), "저장하였습니다.", Toast.LENGTH_SHORT).show();


                    } catch (Exception e) {
                        Log.i(TAG, e.toString());
                        Toast.makeText(getApplicationContext(), "Focusing 실패 다시눌러주세요", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

    }
//
//        btn_start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                tmp=true;
//                writeFlag=true;
//                mViewMode = VIEW_MODE_START;
//                timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
//
//
//
//
//                // 분석할 이미지 생성
//                bmp = null;
//                Mat subimg = mRgba;
//                try {
//                    bmp = Bitmap.createBitmap(subimg.cols(), subimg.rows(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(subimg, bmp);
//                } catch (CvException e) {
//                    Log.d(TAG, e.getMessage());
//                }
//
//
//                // 이미지 촬영후 필요없는 비디오부분 비활성화
//                final Bitmap picture =bmp;
//                mOpenCvCameraView.disableView();
//                mOpenCvCameraView.setVisibility(View.INVISIBLE);
//
//
//
//                // MainActivity가 아니므로 Thread를 통해서 이미지뷰에 이미지 입힘.
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        ImageView iv = (ImageView) findViewById(R.id.imageView1);
//                        iv.setImageBitmap(picture);
//                        Log.i(TAG,"RunOnUIThread");
//                        btn_detect.setEnabled(true);
//                    }
//                });
//                subimg.release();
////                mRgba.release();
//                FileOutputStream out = null;
//
//                long now = System.currentTimeMillis();
//                Date date = new Date(now);
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.-HH:mm:ss", Locale.KOREA);
//                String filename = "Screenshot_" + sdf.format(date) + ".jpg";
//
//
//
//                File sd = new File(Environment.getExternalStorageDirectory().getPath() + "/UrineImages/");
//
//
//
//                boolean success = true;
//                if (!sd.exists()) {
//                    success = sd.mkdir();
//                }
//                if (success) {
//                    File dest = new File(sd, filename);
//                    try {
//                        out = new FileOutputStream(dest);
//
//                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
//
//
//                        // sendBroadCast :: 시스템db에 이미지가 있다는 것을 전달. (나중에 검색하기위해서)
//                        getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dest)));
//                    } catch (NullPointerException e) {
//                        e.printStackTrace();
//                        Log.d(TAG, e.getMessage());
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                        Log.d(TAG, e.getMessage());
//
//                    } finally {
//                        try {
//                            if (out != null) {
//                                out.close();
//                                Log.d(TAG, "OK!!");
//                            }
//                        } catch (IOException e) {
//                            Log.d(TAG, e.getMessage() + "Error");
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//
//
//
//            }
//        });
//
//    }


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

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        frame_num++;
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


                    }

                    if(bubble_array.length==4)
                        doProcess();


                } catch (Exception e) {
                    Intent i =new Intent(getApplicationContext(),MainActivity.class);
                    startActivityForResult(i, Activity.RESULT_OK);
                    Log.i("AnalysisActivityResult", e.toString());
                }
                break;
            case VIEW_MODE_INIT:

                Imgproc.rectangle(mRgba,new Point(900,300),new Point(950,350),new Scalar(255,255,255),2,1);


                break;
        }


        Imgproc.rectangle(mRgba, new Point(cropped_x, cropped_y), new Point(cropped_x + cropped_w, cropped_y + cropped_h), new Scalar(255, 255, 255), 2);



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

        Double array_x[] = new Double[4];
        Double array_y[] = new Double[4];
        Double maxx = 0.0, maxy = 0.0, minx = 10000.0, miny = 10000.0;

        //각 꼭지점 위치 정보 모음 (순서 상관 없음)
        for (int k = 0; k < bubble_array.length; k++) {
            array_x[k] = (bubble_array[k].tl().x + bubble_array[k].br().x) / 2;
            array_y[k] = (bubble_array[k].tl().y + bubble_array[k].br().y) / 2;
        }

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

        double middlex = 0.0, middley = 0.0;
        double[] loc_x = new double[4];
        double[] loc_y = new double[6];

        double distanceX = (sorted_x[1] - sorted_x[0]);

        double distanceY = (sorted_y[1] - sorted_y[0]);
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);


        //각도와 거리비례에 따라 rectangle circle 크기조정 및 회전이동

        double ratio = distance / 1000;
        double angle = calculateAngle();
        middlex = (sorted_x[0] + sorted_x[2]) / 2;
        middley = (sorted_y[0] + sorted_y[2]) / 2;


        double tmpwith=sorted_x[1]-sorted_x[0];




        //각도와 카메라거리에 따라 비색표위치를 tracking하였으나, 사각형의 경우 제대로 반영이 안되어 취소. 원형 비색표이용하면 잘될듯.
        for (int i=0;i<11;i++){
            double x=tmpwith/17.3 * (i) +140;
            double y=0;
            Point sp=new Point(sorted_x[0] + x,sorted_y[0] +20);
            Point np=new Point(sp.x+40,sp.y+350);

            for(int j=0;j<4;j++){
                Point circle_center=new Point(sp.x+20,sp.y+(70*(j+1)));
                Imgproc.circle(mRgba,circle_center,10,new Scalar(255,255,255),2);
                double[] tmp_colorband_RGB= new double[3];
                tmp_colorband_RGB=average_RGB(circle_center.x,circle_center.y);
                colorbandHue[i][j]=calculateHue.getH(tmp_colorband_RGB);

            }


            Imgproc.rectangle(mRgba,sp,np,new Scalar(255,255,255),2);
            Imgproc.circle(mRgba,new Point(sp.x+20,np.y+100),10,new Scalar(255,255,255),2);

            double[] tmp_strip_RGB=new double[3];
            tmp_strip_RGB=average_RGB(sp.x+20,np.y+150);
            stripHue[i]=calculateHue.getH(tmp_strip_RGB);



        }




    }




    //핸드폰 기울어진 각도구함. 두개 object의 위치를 기반으로 각도 계산
    double calculateAngle() {
        double dx = sorted_x[1] - sorted_x[0];
        double dy = sorted_y[1] - sorted_y[0];

        double radian = Math.atan(dy / dx);
        double degree = (double) (57.295779 * radian);

        return radian;
    }


    //그래프표시용 텍스트파일 생성
    private void writeResult() {

        int index=1;
        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd", Locale.KOREA);
        String nowTime = sdf.format(date);

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    ///////////////측정버튼을 누르고 초단위로 동작하는 핸들러//////////////////
    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TIMER_START:


                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT,1000);
                    break;

                case MESSAGE_TIMER_REPEAT:
                        Double avg_hueValue1 = 0.0,avg_hueValue2=0.0,avg_hueValue3=0.0,avg_hueValue4=0.0;

                        try{
                            HashMap<String,Double> trendline =null;
                            double[] tmp_array={0,1,2,3};
                            for(int i=0;i<11;i++) {
                                for(int j=0;j<4;j++) {
                                    //추세선 계산
                                    trendline = calculateHue.getTrendLine(tmp_array, colorbandHue[i]);
                                }


                                //추세선 : y=ax+b     y=소변검사지 농도  x= 농도단계
                                //추세선 기울기 a
                                Double slope=trendline.get("a");

                                //추세선 y절편 b
                                Double intercept=trendline.get("b");



                                // x=(y-b)/a
                                if(!slope.isNaN() && !intercept.isNaN()){
                                    resultIndex[i]=(stripHue[i]-intercept)/slope;
                                    if(resultIndex[i]>5) resultIndex[i]=5;
                                }else{
                                    resultIndex[i]=-1000;
                                }

                                Log.i("AnalysisActivityResult",Double.toString(resultIndex[i]));
                            }

                        }catch (Exception e){
                            Log.i(TAG,e.toString());
                        }




                        // 제대로 인식되었으면 3초 뒤에 Result화면으로
                        if(colorbandHue[0][0]>0){
                            this.sendEmptyMessageDelayed(MESSAGE_TIMER_STOP, 3000);
                        }
                        // 아니면 반복.
                        else {
                            this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 1000);
                        }

                    break;

                case MESSAGE_TIMER_STOP:
//                    Toast.makeText(getApplicationContext(), "Timer Stoped", Toast.LENGTH_SHORT).show();

                    Intent resultItems =new Intent(getApplicationContext(),ResultActivity.class);

                    ArrayList<Double> items=new ArrayList<Double>();

                    //농도단계를 이용해 농도추정후 Result 화면으로 넘김.
                    //INDEX=0 : 잠혈
                    //INDEX=1 : 빌리루빈
                    //INDEX=2 : 우로빌리노겐
                    //INDEX=3 : 케톤체
                    //INDEX=4 : 단백질
                    //INDEX=5 : 아질산염
                    //INDEX=6 : 포도당
                    //INDEX=7 : pH
                    //INDEX=8 : 비중
                    //INDEX=9 : 백혈구
                    //INDEX=10 : 아스코르브산
                    //

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //아직 정확한 농도 계산식은 실험중! 아래 식은 대략적인 값을 나타내며 정확한 값은 좀더 업데이트해야함 ///
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////
                    for(int i=0;i<11;i++){

                        //정상치 넘어가면 -1111로
                        if(resultIndex[i]<0){
                            items.add(-1111.0);
                        }


                        /// 계산식 업데이트필요///
                        else{
                            if(i==0) items.add((resultIndex[i]*50) +10);
                            if(i==1) items.add((resultIndex[i]*10) +9);
                            if(i==2) items.add((resultIndex[i]*2) +2);
                            if(i==3) items.add((resultIndex[i]*20) -10);
                            if(i==4) items.add((resultIndex[i]*60) +10);
                            if(i==5) {
                                if(resultIndex[i]>=2) items.add(1.0);
                            }else{
                                items.add(-1.0);
                            }
                            if(i==6) items.add((resultIndex[i]-1) +100);
                            if(i==7) items.add((resultIndex[i]+4));
                            if(i==8) items.add((resultIndex[i]*10) +1000);
                            if(i==9) items.add((resultIndex[i]*50) -25);
                            if(i==10) items.add((resultIndex[i]*10) -10);
                            /// 계산식 업데이트필요///
                        }
                    }



                    resultItems.putExtra("result",items);


                    startActivity(resultItems);

                    this.removeMessages(MESSAGE_TIMER_REPEAT);



                    break;

            }
        }

    }

    double[] average_RGB(double xx, double yy) {

        int x = (int)xx;
        int y=  (int)yy;
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


}