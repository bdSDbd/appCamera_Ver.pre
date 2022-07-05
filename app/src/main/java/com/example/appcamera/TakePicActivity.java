package com.example.appcamera;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TakePicActivity extends AppCompatActivity {


    private ImageButton button;
    private ImageButton flash , EC , zoom;
    private CameraSurfaceView mCameraSurfaceView;
    private CalldigiMask1 mCalldigiMask1;
    private CameraSurfaceForInput mCameraSurfaceForInput;

    private Activity activity;
    String filePath;
    String qrcodeText;
    String[] qrArray;
    int check;
    private boolean islight=false , isEC = false , isZoom = false;
    private Camera camera = Camera.open();
    private Camera.Parameters parameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        getBundleData();

        qrArray = qrcodeText.split("/");
        System.out.println("*****************************************   "+qrcodeText+"***"+qrArray[0]);

        initSet();
        initView();

        zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isZoom) {
                    isZoom = false;
                    zoom.setImageDrawable(getDrawable(R.drawable.ic_zoom_in));
                    //zoom.setText("普通\n倍率");
                    Toast.makeText(TakePicActivity.this, "普通倍率", Toast.LENGTH_LONG).show();

                    if(qrArray[0].equals("1") && check == 1) {
                        mCalldigiMask1.zoomin();
                        zoom.setImageDrawable(getDrawable(R.drawable.ic_zoom_out));
                        //zoom.setText("放大\n倍率");
                        //Toast.makeText(TakePicActivity.this, "放大倍率", Toast.LENGTH_LONG).show();
                    }
                    else if(qrArray[0].equals("2") && check == 1)
                        mCameraSurfaceView.zoomout();
                    else if(qrArray[0].equals("3") && check == 1)
                        mCameraSurfaceView.zoomout();
                    else
                        mCameraSurfaceForInput.zoomout();
                } else {
                    isZoom = true;
                    zoom.setImageDrawable(getDrawable(R.drawable.ic_zoom_out));
                    //zoom.setText("放大\n倍率");
                    Toast.makeText(TakePicActivity.this, "放大倍率", Toast.LENGTH_LONG).show();

                    if(qrArray[0].equals("1") && check == 1) {
                        mCalldigiMask1.zoomout();
                        zoom.setImageDrawable(getDrawable(R.drawable.ic_zoom_in));
                        //zoom.setText("普通\n倍率");
                        //Toast.makeText(TakePicActivity.this, "普通倍率", Toast.LENGTH_LONG).show();
                    }
                    else if(qrArray[0].equals("2") && check == 1)
                        mCameraSurfaceView.zoomin();
                    else if(qrArray[0].equals("3") && check == 1)
                        mCameraSurfaceView.zoomin();
                    else
                        mCameraSurfaceForInput.zoomin();
                }
            }
        });

        EC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEC) {
                    if(qrArray[0].equals("1") && check == 1)
                        mCalldigiMask1.ECclose();
                    else if(qrArray[0].equals("2") && check == 1)
                        mCameraSurfaceView.ECclose();
                    else if(qrArray[0].equals("3") && check == 1)
                        mCameraSurfaceView.ECclose();
                    else
                        mCameraSurfaceForInput.ECclose();

                    isEC = false;
                    EC.setImageDrawable(getDrawable(R.drawable.ic_ec_on));
                    //EC.setText("自動\n曝光");
                    Toast.makeText(TakePicActivity.this, "自動曝光", Toast.LENGTH_LONG).show();
                } else {
                    if(qrArray[0].equals("1") && check == 1)
                        mCalldigiMask1.ECopen();
                    else if(qrArray[0].equals("2") && check == 1)
                        mCameraSurfaceView.ECopen();
                    else if(qrArray[0].equals("3") && check == 1)
                        mCameraSurfaceView.ECopen();
                    else
                        mCameraSurfaceForInput.ECopen();

                    isEC = true;
                    EC.setImageDrawable(getDrawable(R.drawable.ic_ec_off));
                    //EC.setText("最低\n曝光");
                    Toast.makeText(TakePicActivity.this, "最低曝光", Toast.LENGTH_LONG).show();
                }
            }
        });

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (islight) {
                    if(qrArray[0].equals("1") && check == 1)
                        mCalldigiMask1.fclose();
                    else if(qrArray[0].equals("2") && check == 1)
                        mCameraSurfaceView.fclose();
                    else if(qrArray[0].equals("3") && check == 1)
                        mCameraSurfaceView.fclose();
                    else
                        mCameraSurfaceForInput.fclose();

                    islight = false;
                    flash.setImageDrawable(getDrawable(R.drawable.ic_flash_off));
                    //flash.setText("補光\n燈關");
                    Toast.makeText(TakePicActivity.this, "補光燈關", Toast.LENGTH_LONG).show();

                } else {
                    if(qrArray[0].equals("1") && check == 1)
                        mCalldigiMask1.fopen();
                    else if(qrArray[0].equals("2") && check == 1)
                        mCameraSurfaceView.fopen();
                    else if(qrArray[0].equals("3") && check == 1)
                        mCameraSurfaceView.fopen();
                    else
                        mCameraSurfaceForInput.fopen();

                    islight = true;
                    flash.setImageDrawable(getDrawable(R.drawable.ic_flash_on));
                    //flash.setText("補光\n燈開");
                    Toast.makeText(TakePicActivity.this, "補光燈開", Toast.LENGTH_LONG).show();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setEnabled(false);
                if(qrArray[0].equals("1") && check == 1)
                    mCalldigiMask1.takePicture(activity, filePath);
                else if(qrArray[0].equals("2") && check == 1)
                    mCameraSurfaceView.takePicture(activity, filePath);
                else if(qrArray[0].equals("3") && check == 1)
                    mCameraSurfaceView.takePicture(activity, filePath);
                else
                    mCameraSurfaceForInput.takePicture(activity,filePath);
            }
        });
    }

    private void initSet() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(qrArray[0].equals("1") && check == 1)
            setContentView(R.layout.digimask1);
        else if(qrArray[0].equals("2") && check == 1)
            setContentView(R.layout.activity_take_pic);
        else if(qrArray[0].equals("3") && check == 1)
            setContentView(R.layout.activity_take_pic);
        else
            setContentView(R.layout.maskforinput);
    }

    private void initView() {
        if(qrArray[0].equals("1") && check == 1) {
            mCalldigiMask1 = (CalldigiMask1) findViewById(R.id.CalldigiMask1);
            button = (ImageButton) findViewById(R.id.mask1_takePic);
            /*flash = (Button) findViewById((R.id.flashLight));
            EC = (Button) findViewById((R.id.EC));
            zoom = (Button) findViewById((R.id.zoom));*/
        }
        else if(qrArray[0].equals("2") && check == 1){
            mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);
            button = (ImageButton) findViewById(R.id.takePic);
            /*flash = (Button) findViewById((R.id.flashLight));
            EC = (Button) findViewById((R.id.EC));
            zoom = (Button) findViewById((R.id.zoom));*/
        }
        else if(qrArray[0].equals("3") && check == 1){
            mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);
            button = (ImageButton) findViewById(R.id.takePic);
            /*flash = (Button) findViewById((R.id.flashLight));
            EC = (Button) findViewById((R.id.EC));
            zoom = (Button) findViewById((R.id.zoom));*/
        }
        else{
            mCameraSurfaceForInput = (CameraSurfaceForInput) findViewById(R.id.CameraSurfaceForInput);
            button = (ImageButton) findViewById(R.id.takePicInput);
        }
        flash = (ImageButton) findViewById((R.id.flashLight));
        EC = (ImageButton) findViewById((R.id.EC));
        zoom = (ImageButton) findViewById((R.id.zoom));
    }

    private void getBundleData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            filePath = bundle.getString("url");
            qrcodeText = bundle.getString("qrcode");
            check = bundle.getInt("check");
        }
        Log.d("checkpoint", "check filePath - " + filePath);
    }

    @Override
    public void onBackPressed() {
        /*Intent intent = new Intent();
        intent.setClass(TakePicActivity.this,MainActivity.class);
        startActivity(intent);*/
        activity.setResult(Activity.RESULT_OK);
        activity.finish();

        super.onBackPressed();
    }
}