package com.unydevelopernetwork.opencv_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CircleDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private JavaCameraView cameraView;
    private LinearLayout captureButton;

    public static final String TAG = "src";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.wtf(TAG, "OpenCV Library failed to load!");
        }
    }


    private final BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == SUCCESS) {
                Log.i(TAG, "OpenCV Library loaded successfully");
                cameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_detection);
        initLevel0();
        initLevel1();
        initLevel2();
    }

    private void initLevel0(){
        cameraView = findViewById(R.id.opencv_camera);
        captureButton = findViewById(R.id.capture_button);
    }

    private void initLevel1(){
        getCameraPermission();
        getStoragePermission();
    }

    private void initLevel2(){
        cameraView.setCameraPermissionGranted();
        cameraView.setCvCameraViewListener(this);
    }

    private void getCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void getStoragePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat input = inputFrame.gray();
        Mat circles = new Mat();
        Imgproc.blur(input, input, new Size(7, 7), new Point(2, 2));
        Imgproc.HoughCircles(input, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 90, 0, 1000);

        Log.i(TAG, "size: " + circles.cols() + ", " + circles.rows());

        if (circles.cols() > 0) {
            for (int x=0; x < Math.min(circles.cols(), 1); x++ ) {
                double[] circleVec = circles.get(0, x);

                if (circleVec == null) {
                    break;
                }

                Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                int radius = (int) circleVec[2];

                Imgproc.circle(input, center, 3, new Scalar(0, 255, 0), 10);
                Imgproc.circle(input, center, radius, new Scalar(0, 255, 0), 5);
            }
        }

        circles.release();
        input.release();
        return inputFrame.rgba();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}