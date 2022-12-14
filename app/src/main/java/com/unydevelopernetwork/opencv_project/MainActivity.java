package com.unydevelopernetwork.opencv_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button circleDetection, rectangleDetection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLevel0();
        initLevel1();
    }

    private void initLevel0(){
        circleDetection = findViewById(R.id.btn_circle_camera);
        rectangleDetection = findViewById(R.id.btn_rectangle_camera);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initLevel1(){
        circleDetection.setOnTouchListener((view, motionEvent) -> {
            goToCircleCamera();
            return false;
        });

        rectangleDetection.setOnTouchListener((view, motionEvent) -> {
            goToRectangleCamera();
            return false;
        });
    }

    private void goToCircleCamera(){
        Intent goCircleCamera = new Intent(MainActivity.this, CircleDetection.class);
        goCircleCamera.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(goCircleCamera);
    }

    private void goToRectangleCamera(){
        Intent goRectangleCamera = new Intent(MainActivity.this, RectangleDetection.class);
        goRectangleCamera.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(goRectangleCamera);
    }

}