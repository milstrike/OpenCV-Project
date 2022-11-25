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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class RectangleDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private JavaCameraView cameraView;
    private LinearLayout captureButton;
    private Mat mRGBa, mIntermediateMat, mGray, hierarchy;

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
        setContentView(R.layout.activity_rectangle_detection);
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
        mRGBa = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        hierarchy = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRGBa.release();
        mIntermediateMat.release();
        mGray.release();
        hierarchy.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBa = inputFrame.rgba();
        List<MatOfPoint> contours = new ArrayList<>();
        hierarchy = new Mat();
        Imgproc.Canny(mRGBa, mIntermediateMat, 80, 100);
        Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
        hierarchy.release();

        for(int contourIdx = 0; contourIdx < contours.size(); contourIdx++){
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.01;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = Imgproc.boundingRect(points);
            double height = rect.height;
            double width = rect.width;

            if(height > 300 && width > 300){
                Imgproc.rectangle(mRGBa, new Point(rect.x, rect.y), new Point(rect.x+ rect.width, rect.y+rect.height), new Scalar(0,255, 0, 0), 3);
            }
        }
        return mRGBa;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}