package com.example.appcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private static final String TAG = "CameraSurfaceView";

    private Context mContext;
    private SurfaceHolder holder;
    private Camera mCamera;

    private int mScreenWidth;
    private int mScreenHeight;
    private CameraTopRectView topView;
    int mostW = 0,mostH = 0,preZoom = 5;

    //更動
    private String filePath;
    private Activity activity;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getScreenMetrix(context);

        topView = new CameraTopRectView(context, attrs);

        initView();
    }

    //拿到手機螢幕大小
    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
        System.out.println("----------------------");
        System.out.println("手機螢幕大小:");
        System.out.println(mScreenWidth);
        System.out.println(mScreenHeight);
        System.out.println("----------------------");
    }

    private void initView() {
        holder = getHolder();//获得surfaceHolder引用
        holder.addCallback(this);
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置类型

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        if (mCamera == null) {
            Bitmap bmp = null;
            mCamera = Camera.open();//开启相机
            try {
                mCamera.setPreviewDisplay(holder);//摄像头画面显示在Surface上
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");

        setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        //setCameraParams(mCamera, width, height);
        mCamera.startPreview();

//        mCamera.takePicture(null, null, jpeg);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mCamera.stopPreview();//停止预览
        mCamera.release();//释放相机资源
        mCamera = null;
        holder = null;
    }

    @Override
    public void onAutoFocus(boolean success, Camera Camera) {
        if (success) {
            Log.i(TAG, "onAutoFocus success=" + success);
            System.out.println(success);
        }
    }


    private void setCameraParams(Camera camera, int width, int height) {
        System.out.println("----------------------");
        System.out.println("setcamera:");
        System.out.println(width);
        System.out.println(height);
        System.out.println("----------------------");
        Log.i(TAG, "setCameraParams  width=" + width + "  height=" + height);
        Camera.Parameters parameters = mCamera.getParameters();
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        /*从列表中选取合适的分辨率*/
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            Log.i(TAG, "null == picSize");
            picSize = parameters.getPictureSize();
        }
        Log.i(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        float w = height;//picSize.width;
        float h = width;//picSize.height;
        //parameters.setPictureSize(picSize.width, picSize.height);
        parameters.setPictureSize(mostW, mostH);
        System.out.println("----------------------");
        System.out.println("picsize's w & h:");
        System.out.println(picSize.width);
        System.out.println(picSize.height);
        System.out.println("----------------------");
        this.setLayoutParams(new FrameLayout.LayoutParams((int) (height * (h / w)), height));

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        int qwe = 0;
        for (Camera.Size size : previewSizeList) {
            Log.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);

            //Tequila++
            //if(qwe==14) {
                mostW = 1920;
                mostH = 1080;
            //}
            qwe++;
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(mostW, mostH);
            //parameters.setPreviewSize(preSize.width, preSize.height);
        }

        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }

        mCamera.cancelAutoFocus();//自动对焦。
        mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setParameters(parameters);
    }


    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        System.out.println("result w&h" +result.width +result.height);
        result.width = mostW;
        result.height = mostH;

        return result;
    }

    // 拍照瞬间调用
    private Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.i(TAG, "shutter");
            System.out.println("执行了吗+1");
        }
    };

    // 获得没有压缩过的图片数据
    private Camera.PictureCallback raw = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            Log.i(TAG, "raw");
            System.out.println("执行了吗+2");
        }
    };

    //创建jpeg图片回调数据对象
    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        private Bitmap bitmap;

        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {

            topView.draw(new Canvas());

            BufferedOutputStream bos = null;
            Bitmap bm = null;
            if (data != null) {

            }

            try {
                // 获得图片
                bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.d("checkpoint", "checkpoint - " + bm);
//                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
//                    String filePath = "/sdcard/dyk" + System.currentTimeMillis() + ".JPEG";//照片保存路径

//                    //图片存储前旋转
                Matrix m = new Matrix();
                int height = bm.getHeight();
                int width = bm.getWidth();
                System.out.println("----------------------");
                System.out.println("bm's w & h:");
                System.out.println(bm.getWidth());
                System.out.println(bm.getHeight());
                System.out.println("----------------------");
                m.setRotate(90);
                //旋转后的图片
                //bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, m, true);
                bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, m, true);

                System.out.println("执行了吗+3");
                System.out.println(filePath);
                File file = new File(filePath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));

                Bitmap sizeBitmap = Bitmap.createScaledBitmap(bitmap,
                        topView.getViewWidth(), topView.getViewHeight(), true);
                bm = Bitmap.createBitmap(sizeBitmap, topView.getRectLeft(),
                        topView.getRectTop(),
                        topView.getRectRight() - topView.getRectLeft(),
                        topView.getRectBottom() - topView.getRectTop());// 截取


                bm.compress(Bitmap.CompressFormat.PNG, 100, bos);//将图片压缩到流中

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    bos.flush();//输出
                    bos.close();//关闭
                    bm.recycle();// 回收bitmap空间
                    mCamera.stopPreview();// 关闭预览
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
//                  mCamera.startPreview();// 开启预览
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    public void takePicture(Activity activity, String filePath) {
        this.filePath = filePath;
        this.activity = activity;

        //设置参数,并拍照
        setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        // 当调用camera.takePiture方法后，camera关闭了预览，这时需要调用startPreview()来重新开启预览
        mCamera.takePicture(null, null, jpeg);
        //mCamera.takePicture(shutter, raw, jpeg);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //開啟閃光燈模改
    public void fopen() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
    }

    public void fclose() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF); //取得Camera的參數
        mCamera.setParameters(parameters);
        //mCamera.release(); //使用完記得釋放掉
    }

    //曝光度調整
    public void ECopen() {
        Camera.Parameters parameters = mCamera.getParameters();
        //parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
        parameters.setExposureCompensation(parameters.getMinExposureCompensation());
        mCamera.setParameters(parameters);
    }

    public void ECclose() {
        Camera.Parameters parameters = mCamera.getParameters();
        //parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        parameters.setExposureCompensation((int)parameters.getExposureCompensationStep());
        mCamera.setParameters(parameters);
        //mCamera.release(); //使用完記得釋放掉
    }

    //開啟zoom
    public void zoomin() {
        if(mCamera!=null) {
            Camera.Parameters parameters = mCamera.getParameters();

            if(parameters.isZoomSupported()) {
                int MAX_ZOOM = parameters.getMaxZoom();
                int currnetZoom = parameters.getZoom();
                System.out.println(MAX_ZOOM);
                System.out.println(currnetZoom);
                if(currnetZoom ==0) {
                    currnetZoom+=preZoom;
                    parameters.setZoom(currnetZoom);
                }
            }
            /*else
                Toast.makeText(this, "Zoom Not Avaliable", Toast.LENGTH_LONG).show();*/

            mCamera.setParameters(parameters);
        }
    }

    public void zoomout() {
        if(mCamera!=null) {
            Camera.Parameters parameters = mCamera.getParameters();

            if(parameters.isZoomSupported()) {
                int MAX_ZOOM = parameters.getMaxZoom();
                int currnetZoom = parameters.getZoom();
                System.out.println(MAX_ZOOM);
                System.out.println(currnetZoom);
                if(currnetZoom <=MAX_ZOOM && currnetZoom >0) {
                    currnetZoom-=preZoom;
                    parameters.setZoom(currnetZoom);
                }
            }
            /*else
                Toast.makeText(this, "Zoom Not Avaliable", Toast.LENGTH_LONG).show();*/

            mCamera.setParameters(parameters);
        }
    }
}