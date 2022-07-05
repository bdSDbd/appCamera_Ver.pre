package com.example.appcamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.SensorEventListener;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.detector.FinderPattern;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static {
        if (OpenCVLoader.initDebug()) {
            android.util.Log.d("MainActivity", "OpenCV is loaded");
        } else {
            android.util.Log.d("MainActivity", "OpenCV is not loaded");
        }
    }

    private Activity activity;
    public static final int PermissionCode = 1000;
    public static final int GetPhotoCode = 1001;

    private Button mBtnPic, mInput, mAllInput;

    private ImageView mShowImage;
    private TextView mText, mCount, mCnum, mno;
    private ImageButton mMarked, mSendscv;

    String imageFilePath;


    String CSVFilePath = "0";

    private boolean isCameraPermission = false;

    //泓開發
    public static String qrcodeText = "4/611006003/3";
    String beforeQR = "4/611006003";
    int checkqrtimes = 0, delaytime = 10000, checktimes = 0;
    int checkcircle = 0, checkline = 0, checkqr = 0, checkset = 0;
    int circlex = 0, circley = 0, r = 0;
    int qrx = 0, qry = 0;
    double startAngle = 50, endAngle = 310, startNum = 0, endNum = 60;
    double lx1, ly1, lx2, ly2, d = 0;

    //志開發用
    List<String> ChannelNum = new ArrayList<>();
    List<String> ReadNum = new ArrayList<>();
    int ScanCount = 0;

    int No = 1;
    String Gauge_Code, Channel_Number, Value, Unit;

    public static String[] uuu = new String[]{"psi", "kg/cm^2", "bar", "V", "A", "W", "other"};

    //Seekbar用
    int hsv_v_val, hsv_s_val;

    //CSV用
    public String add_to_csv() {
        //"編號", "拍攝時間", "錶編號", "通道號碼", "數值", "單位"
        String TimeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String st = No + ", " + TimeStamp + ", " + Gauge_Code + ", " + Channel_Number + ", " + Value + ", " + Unit + ", " + "\n";
        return st;
    }

    public static String Units(String st) {
        String unit = "";
        switch (st) {
            case "1":
                unit = uuu[0];
                break;
            case "2":
                unit = uuu[1];
                break;
            case "3":
                unit = uuu[2];
                break;
            case "4":
                unit = uuu[3];
                break;
            case "5":
                unit = uuu[4];
                break;
            case "6":
                unit = uuu[5];
                break;
            default:
                unit = uuu[6];
        }
        return unit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        initView();
        initListener();
        //mSendscv.setEnabled(false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                Toast.makeText(this, "設定", Toast.LENGTH_SHORT).show();
                checkset = 0;
                getSetting();

                break;
            default:
        }
        return true;
    }

    public void ischeck() {
        if (checkset == 1)
            Toast.makeText(this, "儲存設定", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "未變更設定", Toast.LENGTH_SHORT).show();
    }

    public void getSetting() {
        Dialog dialog = new Dialog(MainActivity.this);
        //自定義的dialog.xml
        View viewdialog = getLayoutInflater().inflate(R.layout.settings, null);
        //連接到dialog上面
        dialog.setContentView(viewdialog);
        dialog.setCanceledOnTouchOutside(false);

        //設定Dialog視窗大小
        Display display = getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (display.getWidth() * 0.9);
        dialog.getWindow().setAttributes(params);

        //綁定id要綁上面的view
        Switch sw = viewdialog.findViewById(R.id.setup_switch1);
        Spinner mt = viewdialog.findViewById(R.id.setup_spinner1);

        Button ok = viewdialog.findViewById(R.id.ok);
        Button cancel = viewdialog.findViewById(R.id.cancle);
        Integer[] ttt = new Integer[]{5, 8, 10, 15, 20};

        if (mAllInput.getVisibility() == View.GONE)
            sw.setChecked(true);
        else if (mAllInput.getVisibility() == View.VISIBLE)
            sw.setChecked(false);

        //跟AlterDialog一樣要.show喔不然會沒有顯示出來
        dialog.show();

        ArrayAdapter<Integer> adapter =
                new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, ttt);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        mt.setAdapter(adapter);
        //mt.setSelection(mt.getSelectedItemPosition());

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("sw = " + sw.isChecked());
                System.out.println("mt = " + ttt[mt.getSelectedItemPosition()]);

                if (sw.isChecked()) {
                    mAllInput.setVisibility(View.GONE);
                    mInput.setTextSize(24);
                } else {
                    mAllInput.setVisibility(View.VISIBLE);
                    mInput.setTextSize(20);
                }

                delaytime = ttt[mt.getSelectedItemPosition()] * 1000;
                checkset = 1;

                ischeck();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ischeck();
                dialog.dismiss();
            }
        });
    }

    private void initView() {
        mBtnPic = (Button) findViewById(R.id.btn_take_pic);
        mSendscv = (ImageButton) findViewById(R.id.btn_send);
        mSendscv.setVisibility(View.GONE);
        mShowImage = (ImageView) findViewById(R.id.show_image);
        mText = (TextView) findViewById(R.id.number);
        mCnum = (TextView) findViewById(R.id.Cnumber);
        mno = (TextView) findViewById(R.id.no);
        mInput = (Button) findViewById(R.id.btn_input);
        mAllInput = (Button) findViewById(R.id.btn_allinput);
        mAllInput.setVisibility(View.GONE);
        mCount = (TextView) findViewById(R.id.Count_textView);
        mMarked = (ImageButton) findViewById(R.id.markedup);
        mMarked.setEnabled(false);

        checktimes = 0;
        mText.setEnabled(false);

        mCnum.setVisibility(View.GONE);
        mno.setVisibility(View.GONE);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "QRCode掃描失敗", Toast.LENGTH_LONG).show();
                    checktimes = 0;
                    // forallinput();
                } else {
                    Toast.makeText(MainActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    //qrcodeText = result.getContents();
                    setqrtext(result.getContents());

                    String[] qrArray;
                    qrArray = qrcodeText.split("/");
                    if (!qrArray[0].equals("1") && !qrArray[0].equals("2") && !qrArray[0].equals("3")) {
                        Toast.makeText(MainActivity.this, "Invalid QRCode", Toast.LENGTH_LONG).show();
                        return;
                    }

                    checktimes = 1;
                    mText.setEnabled(true);

                    if (qrArray[0].equals("1") && qrArray[1].equals("1")) {
                        mCnum.setEnabled(true);
                        mCnum.setVisibility(View.VISIBLE);
                    }

                    mno.setVisibility(View.VISIBLE);

                    mInput.setText("跳過\n此錶");
                    mBtnPic.setText("重新\n拍攝");

                    if (qrArray[0].equals("1")) {
                        //if (qrArray[1].equals("1")) {
                        Gauge_Code = qrArray[15];
                        Channel_Number = qrArray[1];
                        Unit = Units(qrArray[14]);
                    } else if (qrArray[0].equals("2")) {
                    } else if (qrArray[0].equals("3")) {
                        Gauge_Code = qrArray[6];
                        Channel_Number = "";
                        Unit = Units(qrArray[5]);
                    }
                    mCnum.setText("通道：" + Channel_Number + " \u270E");
                    mno.setText("錶編號：" + Gauge_Code);

                    if (checkqrtimes < 3) {
                        beforeQR = getqrtext();
                        checkqrtimes = 1;

                        openCamera();
                    } else {
                        checkqrtimes = 0;
                        Dialog dialog = new Dialog(MainActivity.this);
//自定義的dialog.xml
                        View viewdialog = getLayoutInflater().inflate(R.layout.dialog, null);
//連接到dialog上面
                        dialog.setContentView(viewdialog);
                        dialog.setCanceledOnTouchOutside(false);
//設定Dialog視窗大小
                        Display display = getWindowManager().getDefaultDisplay();
                        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                        params.width = (int) (display.getWidth() * 0.9);
                        dialog.getWindow().setAttributes(params);
//綁定id要綁上面的view
                        EditText minin = viewdialog.findViewById(R.id.inin);
                        Button ok = viewdialog.findViewById(R.id.ok);
                        Button cancel = viewdialog.findViewById(R.id.cancle);
//跟AlterDialog一樣要.show喔不然會沒有顯示出來
                        dialog.show();
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!minin.getText().toString().equals("")) {
                                    System.out.println(minin.getText().toString());
                                    mText.setText("數值 : " + minin.getText().toString() + " \u270E");
                                    Value = minin.getText().toString();
                                }
                                //mSendscv.setEnabled(true);
                                mSendscv.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                                //qrcodeText = "4/";
                                checktimes = 2;
                                openCamera();
                            }
                        });
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                }
            });

    private void forallinput() {
        Dialog dialog = new Dialog(MainActivity.this);
        //自定義的dialog.xml
        View viewdialog = getLayoutInflater().inflate(R.layout.dialog2, null);
        //連接到dialog上面
        dialog.setContentView(viewdialog);
        dialog.setCanceledOnTouchOutside(false);

        //設定Dialog視窗大小
        Display display = getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (display.getWidth() * 0.9);
        dialog.getWindow().setAttributes(params);

        //綁定id要綁上面的view
        EditText minin = viewdialog.findViewById(R.id.inin);
        EditText mGC = viewdialog.findViewById(R.id.Gauge_code);
        EditText mC = viewdialog.findViewById(R.id.channel);
        Spinner mU = viewdialog.findViewById(R.id.unit_spinner);

        Button ok = viewdialog.findViewById(R.id.ok);
        Button cancel = viewdialog.findViewById(R.id.cancle);

        //跟AlterDialog一樣要.show喔不然會沒有顯示出來
        dialog.show();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, uuu);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        mU.setAdapter(adapter);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!minin.getText().toString().equals("")) {
                    System.out.println(minin.getText().toString());

                    //mText.setTextColor();
                    Gauge_Code = mGC.getText().toString();
                    Value = minin.getText().toString();
                    Unit = uuu[mU.getSelectedItemPosition()];
                    Channel_Number = mC.getText().toString();

                    mText.setVisibility(View.VISIBLE);
                    mno.setVisibility(View.VISIBLE);
                    mCnum.setVisibility(View.VISIBLE);

                    mText.setText("數值 : " + Value);
                    mCnum.setText("通道：" + Channel_Number);
                    mno.setText("錶編號：" + Gauge_Code);

                    //mSendscv.setEnabled(true);
                    mSendscv.setVisibility(View.VISIBLE);
                }
                dialog.dismiss();
                qrcodeText = "4/";
                openCamera();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void makepath() {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd",
                        Locale.getDefault()).format(new Date());
        String CSVFileName = "CSV_" + timeStamp + ".csv";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        CSVFilePath = storageDir.getAbsolutePath() + "/" + CSVFileName;
    }

    private void initListener() {
        mText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(MainActivity.this);
                //自定義的dialog.xml
                View viewdialog = getLayoutInflater().inflate(R.layout.dialog, null);
                //連接到dialog上面
                dialog.setContentView(viewdialog);
                dialog.setCanceledOnTouchOutside(false);

                //設定Dialog視窗大小
                Display display = getWindowManager().getDefaultDisplay();
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = (int) (display.getWidth() * 0.9);
                dialog.getWindow().setAttributes(params);

                //綁定id要綁上面的view
                EditText minin = viewdialog.findViewById(R.id.inin);
                Button ok = viewdialog.findViewById(R.id.ok);
                Button cancel = viewdialog.findViewById(R.id.cancle);
                //跟AlterDialog一樣要.show喔不然會沒有顯示出來
                dialog.show();
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!minin.getText().toString().equals("")) {
                            System.out.println(minin.getText().toString());

                            String[] qrArray;
                            qrArray = qrcodeText.split("/");

                            if (qrArray[0].equals("1")) {
                                //if (qrArray[1].equals("1")) {
                                Gauge_Code = qrArray[15];
                                Value = minin.getText().toString();
                                Unit = Units(qrArray[14]);
                                            /*}
                                            else if (qrArray[1].equals("2")) {

                                            }*/
                            } else if (qrArray[0].equals("2")) {

                            } else if (qrArray[0].equals("3")) {
                                Gauge_Code = qrArray[6];
                                Value = minin.getText().toString();
                                Unit = Units(qrArray[5]);
                            }
                            mText.setText("數值 : " + minin.getText().toString() + Unit + " \u270E");
                        }
                        //mSendscv.setEnabled(true);
                        mSendscv.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                        //qrcodeText = "4/";
                        /*checktimes = 2;
                        openCamera();*/
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        mCnum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(MainActivity.this);
                //自定義的dialog.xml
                View viewdialog = getLayoutInflater().inflate(R.layout.dialog, null);
                TextView intext = viewdialog.findViewById(R.id.hin);
                intext.setText("請輸入通道數");
                //連接到dialog上面
                dialog.setContentView(viewdialog);
                dialog.setCanceledOnTouchOutside(false);

                //設定Dialog視窗大小
                Display display = getWindowManager().getDefaultDisplay();
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = (int) (display.getWidth() * 0.9);
                dialog.getWindow().setAttributes(params);

                //綁定id要綁上面的view
                EditText minin = viewdialog.findViewById(R.id.inin);
                Button ok = viewdialog.findViewById(R.id.ok);
                Button cancel = viewdialog.findViewById(R.id.cancle);
                //跟AlterDialog一樣要.show喔不然會沒有顯示出來
                dialog.show();
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!minin.getText().toString().equals("")) {
                            System.out.println(minin.getText().toString());
                            mCnum.setText("通道 : " + minin.getText().toString() + " \u270E");

                            Channel_Number = minin.getText().toString();
                            /*String[] qrArray;
                            qrArray = qrcodeText.split("/");

                            if (qrArray[0].equals("1")) {
                                //if (qrArray[1].equals("1")) {
                                Gauge_Code = qrArray[15];
                                Channel_Number = (String) mCnum.getText();
                                Value = minin.getText().toString();
                                Unit = Units(qrArray[14]);
                                            //}
                                            //else if (qrArray[1].equals("2")) {

                                            //}
                            } else if (qrArray[0].equals("2")) {

                            } else if (qrArray[0].equals("3")) {
                                Gauge_Code = qrArray[6];
                                Channel_Number = (String) mCnum.getText();
                                Value = minin.getText().toString();
                                Unit = Units(qrArray[5]);
                            }*/
                        }
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        mBtnPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checktimes == 0) {
                    ScanOptions options = new ScanOptions();
                    options.setTimeout(delaytime);
                    options.setOrientationLocked(false);
                    options.setBeepEnabled(false);
                    barcodeLauncher.launch(options);
                } else {
                    checktimes = 1;
                    openCamera();
                }
            }
        });

        mSendscv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("儲存結果")
                        .setMessage("確認儲存結果?")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ScanOptions options = new ScanOptions();
                                options.setOrientationLocked(false);

                                System.out.println("Send");

                                makepath();
                                save_csv(add_to_csv());
                                readcsv();
                                No++;

                                //mSendscv.setEnabled(false);
                                mSendscv.setVisibility(View.GONE);
                                mMarked.setEnabled(true);
                                Gauge_Code = Channel_Number = Value = Unit = "";

                                qrcodeText = "4/";
                                checktimes = 0;
                                mText.setEnabled(false);
                                mCnum.setEnabled(false);
                                //mCnum.setVisibility(View.GONE);
                                //mno.setVisibility(View.GONE);

                                mBtnPic.setText("自動\n偵測");
                                mInput.setText("QRCode\n掃描");
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                ad.show();
                ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF03DAC5);
                ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFF03DAC5);


            }
        });

        mInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checktimes == 0) {
                    ScanOptions options = new ScanOptions();
                    options.setTimeout(delaytime);
                    options.setOrientationLocked(false);
                    options.setBeepEnabled(false);
                    checkqrtimes = 3;
                    barcodeLauncher.launch(options);
                } else {
                    qrcodeText = "4/";
                    checktimes = 0;
                    mText.setEnabled(false);
                    //mCnum.setVisibility(View.GONE);
                    //mno.setVisibility(View.GONE);

                    mInput.setText("QRCode\n掃描");
                    mBtnPic.setText("自動\n偵測");
                    mSendscv.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "已清除錶頭資訊", Toast.LENGTH_LONG).show();
                }
            }
        });

        mAllInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forallinput();
            }
        });

        mMarked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("錯誤標記")
                        .setMessage("是否確定標記錯誤紀錄?")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //if(new File(CSVFilePath).exists())
                                //makepath();
                                File file = new File(CSVFilePath);
                                StringBuffer cSb = new StringBuffer();
                                String inString, outString = null;

                                if (!file.exists()) {
                                    Toast.makeText(MainActivity.this, "不存在資料", Toast.LENGTH_LONG).show();
                                }
                                try {
                                    BufferedReader reader = new BufferedReader(new FileReader(file));
                                    while ((inString = reader.readLine()) != null) {
                                        cSb.append(inString).append("\n");
                                    }
                                    reader.close();

                                    outString = cSb.toString();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                String[] countStr = outString.split("\n");
                                countStr[(countStr.length - 1)] = countStr[(countStr.length - 1)] + "*";
                                Toast.makeText(MainActivity.this, "Marked", Toast.LENGTH_LONG).show();
                                String st = "";
                                for (int i = 0; i < countStr.length; i++) {
                                    st += (countStr[i] + "\n");
                                }

                                try {
                                    FileOutputStream os = new FileOutputStream(file, false);
                                    os.write(st.getBytes());
                                    os.flush();
                                    os.close();
                                    Toast.makeText(MainActivity.this, "辨識結果以儲存", Toast.LENGTH_LONG).show();
                                    mMarked.setEnabled(false);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                ad.show();
                ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF03DAC5);
                ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFF03DAC5);
            }
        });
    }

    private void readcsv() {
        File file = new File(CSVFilePath);
        StringBuffer cSb = new StringBuffer();
        String inString, outString = null;

        if (!file.exists()) {
            ScanCount = 0;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((inString = reader.readLine()) != null) {
                cSb.append(inString).append("\n");
            }
            reader.close();

            outString = cSb.toString();
            String[] countStr = outString.split("\n");
            ScanCount = countStr.length - 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(outString);


        mCount.setText("今日辨識件數: " + ScanCount + " 件");
        System.out.println(ScanCount);
    }

    private void save_csv(String st) {
        File file = new File(CSVFilePath);
        String title = "No., TimeStamp, Gauge Code, Channel Number, Value, Unit, markup\n";

        if (!file.exists()) {
            try {
                file = createcsvFile();
                FileOutputStream fo = new FileOutputStream(file, true);
                fo.write(title.getBytes());

                fo.flush();
                fo.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream os = new FileOutputStream(file, true);
            os.write(st.getBytes());
            os.flush();
            os.close();
            Toast.makeText(MainActivity.this, "辨識結果以儲存", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionCode) {
            //假如允許了
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isCameraPermission = true;
                //do something
                Toast.makeText(this, "感謝賜予權限！", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(MainActivity.this, TakePicActivity.class), GetPhotoCode);
            }
            //假如拒絕了
            else {
                isCameraPermission = false;
                //do something
                Toast.makeText(this, "CAMERA權限FAIL，請給權限", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //創造檔案名稱、和存擋路徑
    private File createImageFile() throws IOException {
        String[] qrArray;
        qrArray = qrcodeText.split("/");

        if (qrArray.length == 16) {
            Gauge_Code = qrArray[15];
        } else if (qrArray.length == 7) {
            Gauge_Code = qrArray[6];
        } else {
            Gauge_Code = "A";
        }


        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "@" + Gauge_Code + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",   /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        //qwe = Uri.fromFile(image);

        return image;
    }

    private void openCamera() {
        //已獲得權限
        if (isCameraPermission) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.d("checkpoint", "error for createImageFile 創建路徑失敗");
            }
            //成功創建路徑的話
            if (photoFile != null) {
                Intent intent = new Intent(MainActivity.this, TakePicActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", photoFile.getAbsolutePath());
                bundle.putString("qrcode", qrcodeText);
                bundle.putInt("check", checktimes);

                //imageUri = Uri.fromFile(photoFile);
                intent.putExtras(bundle);
                startActivityForResult(intent, GetPhotoCode);
            }
        }
        //沒有獲得權限
        else {
            getPermission();
        }
    }

    private void getPermission() {
        //檢查是否取得權限
        final int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        //沒有權限時
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            isCameraPermission = false;
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionCode);
        } else { //已獲得權限
            isCameraPermission = true;
            openCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GetPhotoCode) {
            try {
                setPic(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPic(String mCurrentPhotoPath) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS", Locale.getDefault());
        String date = sdf.format(new java.util.Date());
        System.out.println(date);

        // Get the dimensions of the View
        int targetW = 800;//mShowImage.getWidth();
        int targetH = 800;//mShowImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        String[] qrArray;
        qrArray = qrcodeText.split("/");

        if (qrArray[0].equals("1") && checktimes == 1) {//數字錶
            ChannelNum = new ArrayList<>();
            ReadNum = new ArrayList<>();

            //重置
            Double DNum = 0d;
            int ChNum = 0;

            if (qrArray[1].equals("1") && checktimes == 1) {//數字錶多通道錶
                mCnum.setVisibility(View.VISIBLE);
                mno.setVisibility(View.VISIBLE);
                try {
                    bitmap = Channel_OCR(bitmap);
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }

                try {
                    bitmap = Digi_OCR(bitmap);
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }

                System.out.println("OCR Done");

                //組合通道數值
                try {
                    for (int i = 0; i < ChannelNum.size(); i++) {
                        ChNum = ChNum * 10 + Integer.parseInt(ChannelNum.get(i));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Catch組合通道數值");
                    e.printStackTrace();
                }


                //組合數值
                try {
                    for (int i = 0; i < ReadNum.size(); i++) {
                        DNum = DNum * 10 + Integer.parseInt(ReadNum.get(i));
                    }
                    if (Integer.parseInt(qrArray[3]) == 0) { //小數位數為零
                        int intNum = (int) Math.round(DNum);
                        Value = Integer.toString(intNum);
                        System.out.println("is 0");
                    } else {
                        DNum = DNum * Math.pow(10, -Integer.parseInt(qrArray[3]));
                        DNum = Math.round(DNum * Math.pow(10, Integer.parseInt(qrArray[3]))) / Math.pow(10, Integer.parseInt(qrArray[3]));
                        Value = String.format("%." + qrArray[3] + "f", DNum);
                        System.out.println("not 0");
                    }

                    System.out.println("DNum" + DNum);
                } catch (NumberFormatException e) {
                    System.out.println("Catch組合數值");
                    e.printStackTrace();
                }


                //csv整理
                Gauge_Code = qrArray[15];
                Channel_Number = Integer.toString(ChNum);
                //Value = Double.toString(DNum);
                Unit = Units(qrArray[14]);
                add_to_csv();

                mText.setText("數值 : " + Value + " " + Unit + " \u270E");
                mCnum.setText("通道：" + Channel_Number + " \u270E");
                mno.setText("錶編號：" + Gauge_Code);
            } else if (qrArray[1].equals("2") && checktimes == 1) {//數字錶單通道錶
                mCnum.setVisibility(View.GONE);
                mno.setVisibility(View.VISIBLE);
                try {
                    bitmap = Digi_OCR(bitmap);
                } catch (Exception e) {
                    System.out.println("Catch digi_ocr");
                    System.out.println(e);
                    return;
                }
                System.out.println("OCR Done");

                //組合通道數值
                ChNum = 0;

                //組合數值
                try {
                    for (int i = 0; i < ReadNum.size(); i++) {
                        DNum = DNum * 10 + Integer.parseInt(ReadNum.get(i));
                    }

                    if (Integer.parseInt(qrArray[3]) == 0) { //小數位數為零
                        int intNum = (int) Math.round(DNum);
                        Value = Integer.toString(intNum);
                        System.out.println("is 0");
                    } else {
                        DNum = DNum * Math.pow(10, -Integer.parseInt(qrArray[3]));
                        DNum = Math.round(DNum * Math.pow(10, Integer.parseInt(qrArray[3]))) / Math.pow(10, Integer.parseInt(qrArray[3]));
                        Value = Double.toString(DNum);
                        System.out.println("not 0");

                    }

                    System.out.println("DNum" + DNum);
                } catch (NumberFormatException e) {
                    System.out.println("Catch組合數值");
                    e.printStackTrace();
                }


                //csv整理
                Gauge_Code = qrArray[15];
                Channel_Number = "";
                //Value = Double.toString(DNum);
                Unit = Units(qrArray[14]);
                add_to_csv();

                mText.setText("數值 : " + Value + " " + Unit + " \u270E");
                mCnum.setText("通道：" + Channel_Number + " \u270E");
                mno.setText("錶編號：" + Gauge_Code);
            }
        } else if (qrArray[0].equals("3") && checktimes == 1) {
            mCnum.setVisibility(View.GONE);
            mno.setVisibility(View.VISIBLE);

            checkqr = checkcircle = checkline = 0;
            circlex = circley = 400;

            startAngle = Double.parseDouble(qrArray[1]);
            endAngle = Double.parseDouble(qrArray[2]);
            startNum = Double.parseDouble(qrArray[3]);
            endNum = Double.parseDouble(qrArray[4]);

            try {
                Bitmap qwe =bitmap.copy(bitmap.getConfig(),true);
                bitmap = detectbarcode(qwe);
            } catch (Exception w) {
                return;
            }
            //bitmap = detectcircle(bitmap);
            //bitmap = detectline(bitmap);

            if (d > 50) {
                Toast.makeText(MainActivity.this, "This is too far", Toast.LENGTH_LONG).show();
                openCamera();
            }
            else if(checkqr == 0){  //沒qr座標重新掃描
                Toast.makeText(MainActivity.this, "拍攝時請盡量垂直對齊錶頭", Toast.LENGTH_LONG).show();
                openCamera();
            }

            //pointXY();

            if (checkqr == 0) {
                qrx = circlex;
                qry = circley + 10;
            }

            double num = calculate();

            int nnn;
            double nb;

            int neg = 0;

            if (num < 0) {
                num *= -1;
                neg = 1;
            }

            if ((int) num / 100 > 0) {
                nnn = (int) num;
                num = nnn;
                System.out.println("3/" + nnn + "/" + num);
            } else if ((int) num / 10 > 0) {
                nb = num * 10;
                nnn = (int) nb;
                nb = nnn;
                num = nb / 10;
                System.out.println("2/" + nnn + "/" + nb + "/" + num);
            } else if (num > 0) {
                nb = num * 100;
                nnn = (int) nb;
                nb = nnn;
                num = nb / 100;
                System.out.println("1/" + nnn + "/" + nb + "/" + num);
            }

            if (neg == 1) {
                num *= -1;
            }


            Gauge_Code = qrArray[6];
            Channel_Number = "";
            int val = (int) num;

            if (qrArray.length > 8) {
                Double ul = Double.parseDouble(qrArray[8]), ll = Double.parseDouble(qrArray[9]);
                if (num < ul || num > ll)
                    Toast.makeText(MainActivity.this, "該筆資料數值需要注意", Toast.LENGTH_LONG).show();
            }

            Value = String.valueOf(num);
            Unit = Units(qrArray[5]);

            mText.setText("數值 : " + num + " " + Unit + " \u270E");
            mCnum.setText("通道：" + Channel_Number + " \u270E");
            mno.setText("錶編號：" + Gauge_Code);

            add_to_csv();
        }
        //mSendscv.setEnabled(true);
        mSendscv.setVisibility(View.VISIBLE);

        mShowImage.setImageBitmap(bitmap);

        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String PicFileName = "PIC_" + timeStamp + "_" + Gauge_Code + ".png";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File PIC = new File(storageDir, PicFileName);
        PIC.createNewFile();

        try {
            FileOutputStream fos = new FileOutputStream(PIC);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        /*System.out.println("----------------------");
        System.out.println("bitmap's w & h:");
        System.out.println(bitmap.getWidth());
        System.out.println(bitmap.getHeight());
        System.out.println("----------------------");*/
        //mShowImage.setImageURI(imageUri);
    }

    public void setqrtext(String qr) {
        qrcodeText = qr;
    }

    public static String getqrtext() {
        return qrcodeText;
    }

    private File createcsvFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd",
                        Locale.getDefault()).format(new Date());
        String CSVFileName = "CSV_" + timeStamp + ".csv";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        File CSV = new File(storageDir, CSVFileName);
        CSV.createNewFile();

        return CSV;
    }


    //辨識器-------------------------------------------------------------------------------------------------------------------------------------------------------
    //圓形指針錶頭
    private void pointXY() {
        Point curPoint = new Point(lx1, ly1);// A點
        Point nextPoint = new Point(lx2, ly2);// B點
        // 求得直線方程式参数y=kx+b
        double k = (curPoint.y - nextPoint.y) * 1.0
                / (curPoint.x - nextPoint.x);// 斜率k
        double b = curPoint.y - k * curPoint.x;// 座標直线b

        circley = (int)(k * circlex + b);
        System.out.println("now circle y is = " + circley);

    }

    private Bitmap detectbarcode(Bitmap bitmap) {
        double sumx = 0, sumy = 0;

        //test
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];

        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);

        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new QRCodeReader();
        double maxx = 0, maxy = 0;
        double minx = 800, miny = 800;
        try {
            Result result = reader.decode(bb);
            System.out.println("text: " + result.getText());
            ResultPoint[] resultPoints = result.getResultPoints();
            System.out.println("resultPoints:");
            /*int rp=2;
            if(resultPoints.length < 2)
                rp = resultPoints.length;
            else if(resultPoints.length >= 2)
                rp = 2;*/
            for (int i = 0; i < resultPoints.length; i++) {
                ResultPoint resultPoint = resultPoints[i];
                System.out.print(" the qr set [" + i + "]:");
                System.out.print(" x = " + resultPoint.getX());
                System.out.print(", y = " + resultPoint.getY());
                sumx += resultPoint.getX();
                sumy += resultPoint.getY();
                if(resultPoint.getX() > maxx)
                    maxx = resultPoint.getX();
                if(resultPoint.getX() < minx)
                    minx = resultPoint.getX();
                if(resultPoint.getY() > maxy)
                    maxy = resultPoint.getY();
                if(resultPoint.getY() < miny)
                    miny = resultPoint.getY();

                if (resultPoint instanceof FinderPattern)
                    System.out.print(", estimatedModuleSize = "
                            + ((FinderPattern) resultPoint).getEstimatedModuleSize());
                System.out.println();
            }
            /*sumx += maxx;
            sumy += maxy;*/
            qrx = (int) (maxx + minx)/2;//sumx / (resultPoints.length);
            qry = (int) (maxy + miny)/2;//sumy / (resultPoints.length);
            System.out.println("qrx = " + qrx);
            System.out.println("qry = " + qry);
            checkqr = 1;
            //byte[] rawBytes = result.getRawBytes();
            //BarcodeFormat format = result.getBarcodeFormat();
            //ResultPoint[] points = result.getResultPoints();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

        bitmap = detectcircle(bitmap);

        return bitmap;
    }

    private Bitmap detectcircle(Bitmap bitmap) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Mat grayMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);

        Utils.bitmapToMat(bitmap, mat);

        int colorChannels = (mat.channels() == 3) ? Imgproc.COLOR_BGR2GRAY : ((mat.channels() == 4) ? Imgproc.COLOR_BGRA2GRAY : 1);
        Imgproc.cvtColor(mat, grayMat, colorChannels);

        /* reduce the noise so we avoid false circle detection */
        Imgproc.GaussianBlur(grayMat, grayMat, new Size(9, 9), 0);
        Imgproc.equalizeHist(grayMat, grayMat);
        //Mat kernel = new Mat(new Size(5, 5), CvType.CV_8UC1, new Scalar(255));
        //Imgproc.morphologyEx(grayMat,grayMat,Imgproc.MORPH_CLOSE,kernel);

        //Imgproc.Canny(grayMat, grayMat, 30, 100);
        //用來區分 strong edge 和 weak edge，範圍都是 0 ~ 255，會在實作過程中進一步討論，通常選擇 threshold2 / threshold1 = 1/2 ~ 1/3，例如 (70, 140), (70, 210)

// accumulator value
        double dp = 1;//.2d;
// minimum distance between the center coordinates of detected circles in pixels
        double minDist = 100;

// min and max radii (set these values as you desire)
        int minRadius = 0, maxRadius = 0;

        double param1 = 150, param2 = 160;

        /* create a Mat object to store the circles detected */
        Mat circles = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);

        /* find the circle in the image */
        Imgproc.HoughCircles(grayMat, circles, Imgproc.CV_HOUGH_GRADIENT, dp, minDist, param1, param2, minRadius, maxRadius);

        /* get the number of circles detected */
        int numberOfCircles = (circles.rows() == 0) ? 0 : circles.cols();

        System.out.println("the circle number:  " + numberOfCircles);
        if (numberOfCircles == 0 || numberOfCircles > 1)
            //return bitmap;
            numberOfCircles = 0;

        // draw the circles found on the image
        for (int i = 0; i < numberOfCircles; i++) {

            double[] circleCoordinates = circles.get(0, i);


            int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];

            Point center = new Point(x, y);

            int radius = (int) circleCoordinates[2];

            // circle's outline
            Imgproc.circle(mat, center, radius, new Scalar(0, 255, 0), 8);

            // circle's center outline
            /*Imgproc.rectangle(mat,
                    new Point(x - 5, y - 5),
                    new Point(x + 5, y + 5),
                    new Scalar(0, 128, 255), -1);*/

            r = radius;

            Imgproc.rectangle(mat, new Point(0, 0), new Point(x - radius, bitmap.getHeight()), new Scalar(255, 255, 255), -1);
            Imgproc.rectangle(mat, new Point(x - radius, 0), new Point(x + radius, y - radius), new Scalar(255, 255, 255), -1);
            Imgproc.rectangle(mat, new Point(x + radius, 0), new Point(bitmap.getWidth(), bitmap.getHeight()), new Scalar(255, 255, 255), -1);
            Imgproc.rectangle(mat, new Point(x - radius, y + radius), new Point(x + radius, bitmap.getHeight()), new Scalar(255, 255, 255), -1);
            for (int cc = 1; cc < 10; cc++)
                Imgproc.circle(mat, center, radius + (cc * 10), new Scalar(255, 255, 255), 10);

            for (int cc = 1; cc < 4; cc++)
                Imgproc.circle(mat, center, radius - (cc * 10), new Scalar(255, 255, 255), 15);

            //center = new Point((center.x+400)/2, (center.y+400)/2);

            Imgproc.circle(mat, center, 20, new Scalar(0, 0, 0, 255), 25);

            circlex = x;
            circley = y;
            System.out.println("circle x and y: " + x + y);

            checkcircle = 1;
        }

        //if (checkcircle == 0) {
        Point center = new Point((400 + circlex) / 2, (400 + circley) / 2);
        Imgproc.circle(mat, center, 20, new Scalar(0, 0, 0, 255), 25);
        for (int cc = 1; cc < 5; cc++)
            Imgproc.circle(mat, center, 320 + (cc * 50), new Scalar(255, 255, 255), 50);
        //}
        //if (checkcircle == 0) {
            circlex = 400;
            circley = 400;
        //}

        Point center1 = new Point(400, 400);
        Imgproc.circle(mat, center1, 20, new Scalar(0, 0, 0, 255), 25);
        for (int cc = 1; cc < 5; cc++)
            Imgproc.circle(mat, center1, 320 + (cc * 50), new Scalar(255, 255, 255), 50);

        /* convert back to bitmap */
        Utils.matToBitmap(mat, bitmap);


        bitmap = detectline(bitmap);

        return bitmap;
    }

    private Bitmap detectline(Bitmap bitmap) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Mat grayMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Mat sss = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Mat lines = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);

        Utils.bitmapToMat(bitmap, mat);

        int colorChannels = (mat.channels() == 3) ? Imgproc.COLOR_BGR2GRAY : ((mat.channels() == 4) ? Imgproc.COLOR_BGRA2GRAY : 1);
        Imgproc.cvtColor(mat, grayMat, colorChannels);

        Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 0);

        //Imgproc.Canny(grayMat, grayMat, 10, 100);
        //Imgproc.HoughLinesP(sss, lines, 1, Math.PI/180, 50, 100, 0);
        //可以用sss = grayMat + Imgproc.threshold(grayMat, sss, 70, 255, Imgproc.THRESH_BINARY_INV) + HoughLinesP(sss, lines, 1, Math.PI/180, 50, 5, 10) 取代上2來抓圓心到指針尖端
        Imgproc.equalizeHist(grayMat, grayMat);
        Imgproc.threshold(grayMat, sss, 10, 255, Imgproc.THRESH_BINARY_INV);
        //Mat kernel = new Mat(new Size(5, 5), CvType.CV_8UC1, new Scalar(255));
        //Imgproc.morphologyEx(sss, sss, Imgproc.MORPH_OPEN, kernel);
        Mat kernel2 = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
        Imgproc.morphologyEx(sss, sss, Imgproc.MORPH_DILATE, kernel2);

        //Imgproc.HoughLinesP(sss, lines, 1, Math.PI / 180, 50, 180, 5);
        Imgproc.HoughLinesP(sss, lines, 1, Math.PI / 180, 50, 180, 5);

        System.out.println("the line col:  " + lines.cols());
        if (lines.cols() == 0 || lines.cols() > 10) {
            Utils.matToBitmap(mat, bitmap);
            return bitmap;
        }

        for (int x = 0; x < lines.cols(); x++) {
            double[] vec = lines.get(0, x);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);

            System.out.println("the start x1 and y1 : (" + x1 + " , " + y1 + ")");
            System.out.println("the end x2 and y2 : (" + x2 + " , " + y2 + ")");
            lx1 = x1;
            lx2 = x2;
            ly1 = y1;
            ly2 = y2;
            Imgproc.line(mat, start, end, new Scalar(0, 128, 255), 3);

            if (checkqr == 0) {
                qrx = circlex;
                qry = circley + 10;
            }

            System.out.println("----------------------checkqr = " + checkqr + "  checkcircle = " + checkcircle);
            System.out.println("circle xy = " + circlex + "  / " + circley);
            //Imgproc.line(mat,new Point(qrx,qry), new Point(circlex,circley), new Scalar(0, 128, 255), 3);

            checkline = 1;
        }

        double k = -((lx1 - circlex) * (lx2 - lx1) + (ly1 - circley) * (ly2 - ly1)) / ((lx2 - lx1) * (lx2 - lx1) + (ly2 - ly1) * (ly2 - ly1));
        double llx = k * (lx2 - lx1) + lx1;
        double lly = k * (ly2 - ly1) + ly1;

        System.out.println("llx、lly = ");
        System.out.println(llx);
        System.out.println(lly);

        System.out.println("Cx、Cy = ");
        System.out.println(circlex);
        System.out.println(circley);

        d = Math.sqrt((circlex - llx) * (circlex - llx) + (circley - lly) * (circley - lly));
        System.out.println("d = " + d);

        Utils.matToBitmap(mat, bitmap);

        return bitmap;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private double getangle(double dist_pt1, double dist_pt2, double x, double y, double x1, double y1, double x2, double y2) {
        double x_angle = 0, y_angle = 0;

        if (dist_pt1 > dist_pt2) {
            x_angle = x1 - x;
            y_angle = y - y1;
        } else {
            x_angle = x2 - x;
            y_angle = y - y2;
        }
        System.out.println("x_ang = " + x_angle);
        System.out.println("y_ang = " + y_angle);

        double ang = Math.atan(y_angle / x_angle);
        ang = Math.toDegrees(ang);
        double fin_ang = 0;
        if (x_angle > 0 && y_angle > 0) {
            fin_ang = 270 - ang;
        } else if (x_angle < 0 && y_angle > 0) {
            fin_ang = 90 - ang;
        } else if (x_angle < 0 && y_angle < 0) {
            fin_ang = 90 - ang;
        } else if (x_angle > 0 && y_angle < 0) {
            fin_ang = 270 - ang;
        }

        return fin_ang;
    }

    private double calculate() {
        double out = 0, ang = 0;
        //用指針的尖端來計算夾角
        double dist_pt1 = dist(circlex, circley, lx1, ly1);
        double dist_pt2 = dist(circlex, circley, lx2, ly2);
        System.out.println("dist1 = " + dist_pt1);
        System.out.println("dist2 = " + dist_pt2);
        ang = getangle(dist_pt1, dist_pt2, circlex, circley, lx1, ly1, lx2, ly2);
        System.out.println("the final angle = " + ang);

        //計算qr code中心夾角
        double qr_ang = 0, fix_ang = 0;
        System.out.println("qrx = " + qrx);
        System.out.println("qry = " + qry);
        dist_pt1 = dist(circlex, circley, circlex, circley + 1);
        dist_pt2 = dist(circlex, circley, qrx, qry);
        System.out.println("qr_dist1 = " + dist_pt1);
        System.out.println("qr_dist2 = " + dist_pt2);
        qr_ang = getangle(dist_pt1, dist_pt2, circlex, circley, circlex, circley + 1, qrx, qry);
        System.out.println("the qr code angle = " + qr_ang);
        if (circlex >= qrx)
            fix_ang = qr_ang;
        else
            fix_ang = 360 - qr_ang;
        System.out.println("the fix angle = " + fix_ang);

        double esa = endAngle - startAngle, esn = endNum - startNum, edn;
        edn = esn / esa;
        System.out.println("each degree number = " + edn);

        ang = ang - startAngle;
        if (circlex >= qrx)
            ang -= fix_ang;
        else
            ang += fix_ang;

        if (ang < 0)
            ang = 0;
        else if (ang > esa)
            ang = esa;

        out = ang * edn + startNum;
        System.out.println("out = " + out);

        //return ang; //如果要改成輸出角度的話
        return out;
    }

    //數字錶頭-------------------------------------------------------------------------------------------------------------------------------------------------------
    public static int channel_color_code() {
        String[] qrArray;
        qrArray = qrcodeText.split("/");
        int code = Integer.parseInt(qrArray[13]); //通道顏色
        return code;
    }

    public static int digi_color_code() {
        String[] qrArray;
        qrArray = qrcodeText.split("/");
        int code = Integer.parseInt(qrArray[12]); //數字顏色
        return code;
    }

    public static Rect channelRect() {
        Rect rect = new Rect();
        String[] qrArray;
        qrArray = qrcodeText.split("/");
        int x = Integer.parseInt(qrArray[8]); //通道x
        int y = Integer.parseInt(qrArray[9]); //通道y
        int w = Integer.parseInt(qrArray[10]); //通道w
        int h = Integer.parseInt(qrArray[11]); //通道h
        rect.x = x;
        rect.y = y;
        rect.width = w;
        rect.height = h;
        System.out.println("channelRect" + rect);

        return rect;
    }

    public static Rect digiRect() {
        Rect rect = new Rect();
        String[] qrArray;
        qrArray = qrcodeText.split("/");
        int x = Integer.parseInt(qrArray[4]); //數值x
        int y = Integer.parseInt(qrArray[5]); //數值y
        int w = Integer.parseInt(qrArray[6]);//數值w
        int h = Integer.parseInt(qrArray[7]);//數值h
        rect.x = x;
        rect.y = y;
        rect.width = w;
        rect.height = h;
        System.out.println("digiRect" + rect);

        return rect;
    }

    public static Mat channel_masked(Mat mat) {
        int x = channelRect().x;
        int y = channelRect().y;
        int w = channelRect().width;
        int h = channelRect().height;
        Scalar scalar = new Scalar(0, 0, 0);
        Rect rect1 = new Rect(0, 0, 800, y);
        Rect rect2 = new Rect(0, (y + h), 800, (800 - (y + h)));
        Rect rect3 = new Rect(0, 0, x, 800);
        Rect rect4 = new Rect((x + w), 0, (800 - (x + w)), 800);
        Imgproc.rectangle(mat, rect1, scalar, -1);
        Imgproc.rectangle(mat, rect2, scalar, -1);
        Imgproc.rectangle(mat, rect3, scalar, -1);
        Imgproc.rectangle(mat, rect4, scalar, -1);

        return mat;
    }

    public static Mat digi_masked(Mat mat) {
        int x = digiRect().x;
        int y = digiRect().y;
        int w = digiRect().width;
        int h = digiRect().height;
        Scalar scalar = new Scalar(0, 0, 0);
        Rect rect1 = new Rect(0, 0, 800, y);
        Rect rect2 = new Rect(0, (y + h), 800, (800 - (y + h)));
        Rect rect3 = new Rect(0, 0, x, 800);
        Rect rect4 = new Rect((x + w), 0, (800 - (x + w)), 800);
        Imgproc.rectangle(mat, rect1, scalar, -1);
        Imgproc.rectangle(mat, rect2, scalar, -1);
        Imgproc.rectangle(mat, rect3, scalar, -1);
        Imgproc.rectangle(mat, rect4, scalar, -1);

        return mat;
    }

    public static HashMap DIGI_LOOKUP() {
        List<Integer> DIGI0 = Arrays.asList(1, 1, 1, 1, 1, 1, 0);
        List<Integer> DIGI1 = Arrays.asList(1, 1, 0, 0, 0, 0, 0);
        List<Integer> DIGI2 = Arrays.asList(1, 0, 1, 1, 0, 1, 1);
        List<Integer> DIGI3 = Arrays.asList(1, 1, 1, 0, 0, 1, 1);
        List<Integer> DIGI4 = Arrays.asList(1, 1, 0, 0, 1, 0, 1);
        List<Integer> DIGI5 = Arrays.asList(0, 1, 1, 0, 1, 1, 1);
        List<Integer> DIGI6 = Arrays.asList(0, 1, 1, 1, 1, 1, 1);
        List<Integer> DIGI6_1 = Arrays.asList(0, 1, 1, 1, 1, 0, 1);
        List<Integer> DIGI7 = Arrays.asList(1, 1, 0, 0, 0, 1, 0);
        List<Integer> DIGI7_1 = Arrays.asList(1, 1, 0, 0, 1, 1, 0);
        List<Integer> DIGI8 = Arrays.asList(1, 1, 1, 1, 1, 1, 1);
        List<Integer> DIGI9 = Arrays.asList(1, 1, 1, 0, 1, 1, 1);
        List<Integer> DIGI9_1 = Arrays.asList(1, 1, 0, 0, 1, 1, 1);
        List<Integer> DIGIminus = Arrays.asList(0, 0, 0, 0, 0, 1, 1);

        HashMap<List, String> DIGITS_LOOKUP = new HashMap<List, String>();
        DIGITS_LOOKUP.put(DIGI0, "0");
        DIGITS_LOOKUP.put(DIGI1, "1");
        DIGITS_LOOKUP.put(DIGI2, "2");
        DIGITS_LOOKUP.put(DIGI3, "3");
        DIGITS_LOOKUP.put(DIGI4, "4");
        DIGITS_LOOKUP.put(DIGI5, "5");
        DIGITS_LOOKUP.put(DIGI6, "6");
        DIGITS_LOOKUP.put(DIGI6_1, "6");
        DIGITS_LOOKUP.put(DIGI7, "7");
        DIGITS_LOOKUP.put(DIGI7_1, "7");
        DIGITS_LOOKUP.put(DIGI8, "8");
        DIGITS_LOOKUP.put(DIGI9, "9");
        DIGITS_LOOKUP.put(DIGI9_1, "9");
        DIGITS_LOOKUP.put(DIGIminus, "-");

        return DIGITS_LOOKUP;
    }

    //---------------------------------------------------------------------------------------------------------------

    private static List<Mat> cut_digi(Mat mat) {
        System.out.println("cut_digi In");
        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        Imgproc.connectedComponentsWithStats(mat, labels, stats, centroids, 4);
        System.out.println("labels: " + labels);
        System.out.println("stats: " + stats);

        return Arrays.asList(labels, stats, centroids);
    }

    private static Rect draw_edge_rect(Mat mat, List<MatOfPoint> cnts) {
        int x1max = 800;
        int y1max = 800;
        int x2max = 0;
        int y2max = 0;
        for (MatOfPoint cnt : cnts) {
            Rect rect = Imgproc.boundingRect(cnt);
            //System.out.println("boundingRect: " + rect);
            if (x1max > rect.x) {
                x1max = rect.x;
            }
            if (y1max > rect.y) {
                y1max = rect.y;
            }
            if (x2max < rect.x + rect.width) {
                x2max = rect.x + rect.width;
            }
            if (y2max < rect.y + rect.height) {
                y2max = rect.y + rect.height;
            }
        }
        Rect edge = new Rect(x1max, y1max, x2max - x1max, y2max - y1max);
        System.out.println("edge:" + edge);
        return edge;
    }

    private static List<Rect> sorted_stats(Mat stats) {
        System.out.println("sorted_stats In");
        List<Rect> output = new ArrayList<>();

        for (int x = 1; x < stats.rows(); x++) {
            int[] state = new int[5];
            stats.row(x).get(0, 0, state);
            Rect rect = new Rect(state[0], state[1], state[2], state[3]);
            output.add(rect);
        }
        System.out.println("before: " + output);

        for (int x = 1; x < output.size(); x++) {
            for (int y = 0; y < output.size(); y++) {
                if (output.get(x).x < output.get(y).x) {
                    Rect temp = output.get(x);
                    output.set(x, output.get(y));
                    output.set(y, temp);
                }
            }
        }
        System.out.println("after: " + output);
        System.out.println("sorted_stats Done");
        return output;
    }

    private static List<Rect> findRect(Mat mat, List<Rect> sorted_stats) {
        List digits_positions = new ArrayList();


        System.out.println("findRect In");

        for (int i = 0; i < sorted_stats.size(); i++) {
            //System.out.println("for In");
            Rect rect = sorted_stats.get(i);
            int area = rect.width * rect.height;
            float h = rect.height;
            float w = rect.width;
            float h_w = h / w;
            System.out.println("area:" + area);

            if (rect.width == rect.height) {
                System.out.println("Exclude with rect.width == rect.height");
                continue;
            } else if (rect.height < rect.width) {
                System.out.println("Exclude with rect.height < rect.width");
                continue;
            } else if (h_w <= 1.2) {
                System.out.println(h_w);
                System.out.println("Exclude with rect.height / rect.width <= 1.2");
                continue;
            } else if (area <= 1200) {
                System.out.println("Exclude with area <= 500");
                continue;
            } else if (h <= 50) {
                System.out.println("Exclude with area <= 500");
                continue;
            }

//            else if (rect.y >= 370) {
//                System.out.println("Exclude with rect.y >= 370");
//                continue;
//            }
//            else if (area >= 17500) {
//                System.out.println("area >= 16000");
//                continue;
//            }

            Mat pre_roi = new Mat(mat, rect);
            Bitmap bmp = Bitmap.createBitmap(pre_roi.width(), pre_roi.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(pre_roi, bmp);

            List<MatOfPoint> cnts = new ArrayList<>();
            Imgproc.findContours(pre_roi, cnts, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
            Rect edge_rect = draw_edge_rect(pre_roi, cnts);
            System.out.println("edge_rect" + edge_rect);
            Rect roi_rect = new Rect();
            roi_rect.x = rect.x + edge_rect.x;
            roi_rect.width = edge_rect.width;
            roi_rect.y = rect.y + edge_rect.y;
            roi_rect.height = edge_rect.height;

            Mat test = new Mat(mat, roi_rect);
            Bitmap bmp_edge = Bitmap.createBitmap(test.width(), test.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(test, bmp_edge);

            System.out.println("roi_rect" + roi_rect);
            digits_positions.add(roi_rect);
        }

        return digits_positions;
    }

    private static List recognize_digits_line_method(List<Rect> digits_positions, Mat Outputmat, Mat Inputmat) {
        HashMap DIGITS_LOOKUP = DIGI_LOOKUP();
        List digits = new ArrayList();
        float H_W_Ratio = 1.5f, arc_tan_theta = 6.0f;
        ;

        System.out.println("recognize_digits_line_method In");

//        Bitmap bmp_input = Bitmap.createBitmap(Inputmat.cols(), Inputmat.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(Inputmat, bmp_input);

        System.out.println("digits_positions:" + digits_positions);
        for (int i = 0; i < digits_positions.size(); i++) {
            System.out.println("digits_positions No. = " + i);
            Rect roiRect = new Rect(digits_positions.get(i).x, digits_positions.get(i).y, digits_positions.get(i).width, digits_positions.get(i).height);
            int suppose_W = Math.max(1, (int) (roiRect.height / H_W_Ratio));

            //對1的情況單獨辨識
            if (roiRect.width < suppose_W / 2 + suppose_W * 0.1) {
                roiRect.x = roiRect.x + roiRect.width - suppose_W;
                roiRect.width = suppose_W;
            }
            System.out.println("Rect: " + roiRect);
            Mat roi = Inputmat.submat(roiRect);

            int h = roi.height();
            int w = roi.width();
            System.out.println("roi setup Done");

            int center_y = (int) Math.floor(h / 2);
            int quater_y_1 = (int) Math.floor(h / 4);
            int quater_y_3 = quater_y_1 * 3;
            int center_x = (int) Math.floor(w / 2);
            int line_width = 3;
            int width = (Math.max((int) (w * 0.15), 1) + Math.max((int) (h * 0.15), 1)) / 2;
            int small_delta = (int) ((h / arc_tan_theta) / 4);
            System.out.println("roi setup Done2");
            List<Integer> seg0_0 = Arrays.asList(w - 2 * width, quater_y_1 - line_width);
            List<Integer> seg0_1 = Arrays.asList(w, quater_y_1 + line_width);
            List<Integer> seg1_0 = Arrays.asList(w - 2 * width, quater_y_3 - line_width);
            List<Integer> seg1_1 = Arrays.asList(w, quater_y_3 + line_width);
            List<Integer> seg2_0 = Arrays.asList(center_x - line_width - small_delta, h - 2 * width);
            List<Integer> seg2_1 = Arrays.asList(center_x - small_delta + line_width, h);
            List<Integer> seg3_0 = Arrays.asList(0, quater_y_3 - line_width);
            List<Integer> seg3_1 = Arrays.asList(2 * width, quater_y_3 + line_width);
            List<Integer> seg4_0 = Arrays.asList(0, quater_y_1 - line_width);
            List<Integer> seg4_1 = Arrays.asList(2 * width, quater_y_1 + line_width);
            List<Integer> seg5_0 = Arrays.asList(center_x - line_width, 0);
            List<Integer> seg5_1 = Arrays.asList(center_x + line_width, 2 * width);
            List<Integer> seg6_0 = Arrays.asList(center_x - line_width, center_y - line_width);
            List<Integer> seg6_1 = Arrays.asList(center_x + line_width, center_y + line_width);

            List<List<Integer>> seg0 = Arrays.asList(seg0_0, seg0_1);
            List<List<Integer>> seg1 = Arrays.asList(seg1_0, seg1_1);
            List<List<Integer>> seg2 = Arrays.asList(seg2_0, seg2_1);
            List<List<Integer>> seg3 = Arrays.asList(seg3_0, seg3_1);
            List<List<Integer>> seg4 = Arrays.asList(seg4_0, seg4_1);
            List<List<Integer>> seg5 = Arrays.asList(seg5_0, seg5_1);
            List<List<Integer>> seg6 = Arrays.asList(seg6_0, seg6_1);

            List<List<List<Integer>>> segments = Arrays.asList(seg0, seg1, seg2, seg3, seg4, seg5, seg6);
            System.out.println("roi setup Done3");

            List<Integer> on = new ArrayList<>();
            for (int k = 0; k < segments.size(); k++) {
                //System.out.println("for on.add In");
                on.add(0);
            }
            //System.out.println("on = " + on);
            //System.out.println("for on.add Out");

            System.out.println("roi setup Done4");
            int segmentsIndex = 0;
            for (List<List<Integer>> segment : segments) {
                int xa = segment.get(0).get(0);
                int ya = segment.get(0).get(1);
                int xb = segment.get(1).get(0);
                int yb = segment.get(1).get(1);
                //System.out.println("get xyab");
                Rect segRect = new Rect(1, 1, 1, 1);

                System.out.println("xa:" + xa + " ,ya:" + ya + " ,xb:" + xb + " ,yb:" + yb);
                System.out.println("roi setup Done4-1");

                if (ya < 0) {
                    ya = 0;
                }
                if (xa < 0) {
                    xa = 0;
                }

                segRect = new Rect(xa, ya, xb - xa, yb - ya);

                System.out.println("get segrect");
                Mat seg_roi = roi.submat(segRect);
                //System.out.println("submit done");
                System.out.println("roi setup Done4-2");

                Mat draw = roi.clone();
                Imgproc.rectangle(draw, segRect, new Scalar(255, 255, 0));
                //System.out.println("submit done");

                int total = Core.countNonZero(seg_roi);
                Float area = (xb - xa) * (yb - ya) * 0.9f;
                if (total / area > 0.25) {
                    on.set(segmentsIndex, 1);
                }
                segmentsIndex++;
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            }
            System.out.println("roi setup Done5");
            //System.out.println("on = " + on);
            //System.out.println("segment Done");

            Object digit = new String();
            boolean flag = DIGITS_LOOKUP.containsKey(on);
            if (flag == true) {
                digit = DIGITS_LOOKUP.get(on);
                digits.add(digit);
            }
//            else {
//                digit = "*";
//            }
            System.out.println("get digit Done");

            Imgproc.rectangle(Outputmat, new Point(roiRect.x, roiRect.y), new Point(roiRect.x + roiRect.width, roiRect.y + roiRect.height), new Scalar(255, 0, 0));
            Imgproc.putText(Outputmat, (String) digit, new Point(roiRect.x + 3, roiRect.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0));
            System.out.println("putText Done");

        }
        System.out.println(digits);
        return digits;
    }

    //通道OCR
    private Bitmap Channel_OCR(Bitmap bitmap) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, mat);

        Mat endmat = mat.clone();

        Mat Masked = channel_masked(mat);
        System.out.println("masked Done");

        List<Mat> prebuild2 = prebuild1(Masked);
        System.out.println("prebuild Done");

        List<Mat> cut_digi = cut_digi(prebuild2.get(0));
        System.out.println("cut_digi Done");

        List<Rect> sorted_stats = sorted_stats(cut_digi.get(1));
        System.out.println("sort_stats Done");

        List<Rect> digits_position = findRect(prebuild2.get(1), sorted_stats);
        System.out.println("findRect Done");

        List digits = null;

        try {
            digits = recognize_digits_line_method(digits_position, endmat, prebuild2.get(1));
            System.out.println("recognize_digits_line_method Done");

        } catch (Exception e) {
            e.printStackTrace();
        }

        ChannelNum = digits;
        System.out.println("ChannelNum: " + ChannelNum);
        System.out.println("ChannelNum Done");

        Utils.matToBitmap(endmat, bitmap);
        //Utils.matToBitmap(prebuild2.get(1), bitmap);
        return bitmap;
    }

    //數值OCR
    private Bitmap Digi_OCR(Bitmap bitmap) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, mat);

        Mat endmat = mat.clone();

        Mat Masked = digi_masked(mat);
        System.out.println("masked Done");

        Bitmap bmp1 = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(Masked, bmp1);

        List<Mat> prebuild2 = prebuild2(Masked);
        System.out.println("prebuild Done");

        List<Mat> cut_digi = cut_digi(prebuild2.get(0));
        System.out.println("cut_digi Done");

        List<Rect> sorted_stats = sorted_stats(cut_digi.get(1));
        System.out.println("sort_stats Done");

        List<Rect> digits_position = findRect(prebuild2.get(1), sorted_stats);
        System.out.println("findRect Done");

        List digits = null;

        try {
            digits = recognize_digits_line_method(digits_position, endmat, prebuild2.get(1));
            System.out.println("recognize_digits_line_method Done");

        } catch (Exception e) {
            e.printStackTrace();
        }

        ReadNum = digits;
        System.out.println("ReadNum: " + ReadNum);
        System.out.println("ReadNum Done");

        Utils.matToBitmap(endmat, bitmap);
        return bitmap;
    }

    private static List<Scalar> SS_color(int i) {
        Scalar mincolor;
        Scalar maxcolor;

        if (i == 1) { //紅色
            mincolor = new Scalar(0, 0, 220);
            maxcolor = new Scalar(155, 255, 255);
        } else if (i == 2) { //綠色
            mincolor = new Scalar(40, 58, 190);
            maxcolor = new Scalar(95, 255, 255);
        } else if (i == 3) { //藍色
            mincolor = new Scalar(15, 0, 200);
            maxcolor = new Scalar(160, 255, 255);
        } else if (i == 4) { //黃色
            mincolor = new Scalar(0, 0, 10);
            maxcolor = new Scalar(129, 255, 255);
        } else {
            mincolor = new Scalar(0, 0, 0);
            maxcolor = new Scalar(255, 255, 255);
        }
        List output = Arrays.asList(mincolor, maxcolor);

        return output;
    }

    private List<Mat> prebuild1(Mat mat) {
        Mat open_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilate_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 11));

        Mat hsv = new Mat();
        Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);

        Imgproc.GaussianBlur(mat, mat, new Size(11, 11), 0, 0);

        Scalar min = SS_color(channel_color_code()).get(0);
        Scalar max = SS_color(channel_color_code()).get(1);
        System.out.println("min: " + min);
        System.out.println("max: " + max);

        Mat mask = new Mat();
        Core.inRange(hsv, min, max, mask);

        Bitmap bmp1 = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mask, bmp1);

        Mat open = new Mat();
        Imgproc.morphologyEx(mask, open, Imgproc.MORPH_OPEN, open_kernel);

        Bitmap bmp2 = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(open, bmp2);

        Mat dilate = new Mat();
        Imgproc.dilate(open, dilate, dilate_kernel, new Point(-1, -1), 2);

        Bitmap bmp3 = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dilate, bmp3);

        List output = Arrays.asList(dilate, open);


        return output;
    }

    private List<Mat> prebuild2(Mat mat) {
        Mat open_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilate_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 11));

        Mat hsv = new Mat();
        Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);

        Imgproc.GaussianBlur(mat, mat, new Size(11, 11), 0, 0);

        Scalar min = SS_color(digi_color_code()).get(0);
        Scalar max = SS_color(digi_color_code()).get(1);
        System.out.println("min: " + min);
        System.out.println("max: " + max);

        Mat mask = new Mat();
        Core.inRange(hsv, min, max, mask);

        Bitmap bmp1 = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mask, bmp1);

        Mat open = new Mat();
        Imgproc.morphologyEx(mask, open, Imgproc.MORPH_OPEN, open_kernel);

        Bitmap bmp2 = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(open, bmp2);

        Mat dilate = new Mat();
        Imgproc.dilate(open, dilate, dilate_kernel, new Point(-1, -1), 2);

        Bitmap bmp3 = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dilate, bmp3);

        List output = Arrays.asList(dilate, open);


        return output;
    }
}