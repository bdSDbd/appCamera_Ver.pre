package com.example.appcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class
DrawdigiMask1 extends View {

    private int panelWidth;
    private int panelHeght;

    private int viewWidth;
    private int viewHeight;

    public int rectWidth;
    public int rectHeght;

    private int rectTop;
    private int rectLeft;
    private int rectRight;
    private int rectBottom;

    private int lineLen;
    private int lineWidht;
    private static final int LINE_WIDTH = 15;
    private static final int TOP_BAR_HEIGHT = 50;
    private static final int BOTTOM_BTN_HEIGHT = 66;


    private static final int LEFT_PADDING = 10;
    private static final int RIGHT_PADDING = 10;

    String mtext = MainActivity.getqrtext();
    org.opencv.core.Rect channelRect = MainActivity.channelRect();
    org.opencv.core.Rect digiRect = MainActivity.digiRect();

    int digi_color_code = MainActivity.digi_color_code();
    int channel_color_code = MainActivity.channel_color_code();

    private static final String TIPS = "請對準數位錶";

    private Paint linePaint;
    private Paint wordPaint;
    private Rect rect;
    private int baseline;

    public DrawdigiMask1(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        Activity activity = (Activity) context;

        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        panelWidth = wm.getDefaultDisplay().getWidth();//拿到螢幕的寬
        panelHeght = wm.getDefaultDisplay().getHeight();//拿到螢幕的高
        System.out.println("----------------------");
        System.out.println("top rectview:");
        System.out.println(panelWidth);
        System.out.println(panelHeght);
        System.out.println("----------------------");

        //高度不需要dp轉換px,不然整體相機會向上移動一小節
//        viewHeight = panelHeght - (int) DisplayUtil.dp2px(activity,TOP_BAR_HEIGHT + BOTTOM_BTN_HEIGHT);

        viewHeight = panelHeght;
        //viewHeight,界面的高,viewWidth,界面的寬
        viewWidth = panelWidth;

        rectWidth = 800;//panelWidth - (int) DisplayUtil.dp2px(activity,LEFT_PADDING + RIGHT_PADDING);
        //rectWidth = (int)panelWidth;

        //rectHeght = (int) (rectWidth * 54 / 85.6);
        rectHeght = 800;
        //rectHeght = (int) (rectWidth);
        // 相對於此view，拍攝的位置(但會偏移?
        rectTop = (viewHeight - rectHeght) / 2;
        rectLeft = (viewWidth - rectWidth) / 2;
        rectBottom = rectTop + rectHeght;
        rectRight = rectLeft + rectWidth;

        lineLen = panelWidth / 8;

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.rgb(0xff, 0x24, 0x50));
        linePaint.setStyle(Style.STROKE);
        linePaint.setStrokeWidth(LINE_WIDTH);// 線寬
        linePaint.setAlpha(255);

        wordPaint = new Paint();
        wordPaint.setAntiAlias(true);
        wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wordPaint.setStrokeWidth(3);
        wordPaint.setTextSize(35);

        rect = new Rect(rectLeft, rectTop - 80, rectRight, rectTop - 10);//提示文字的位置
        FontMetricsInt fontMetrics = wordPaint.getFontMetricsInt();
        baseline = rect.top + (rect.bottom - rect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        wordPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wordPaint.setColor(Color.TRANSPARENT);
        canvas.drawRect(rect, wordPaint);

        //画蒙层
        wordPaint.setColor(0xa0000000);
        rect = new Rect(0, viewHeight/2+rectHeght/2, viewWidth, viewHeight);
        canvas.drawRect(rect, wordPaint);

        rect = new Rect(0, 0, viewWidth, viewHeight/2-rectHeght/2);
        canvas.drawRect(rect, wordPaint);

        rect = new Rect(0, viewHeight/2-rectHeght/2, (viewWidth-rectWidth)/2, viewHeight/2+rectHeght/2);
        canvas.drawRect(rect, wordPaint);

        rect = new Rect(viewWidth-(viewWidth-rectWidth)/2, viewHeight/2-rectHeght/2, viewWidth, viewHeight/2+rectHeght/2);
        canvas.drawRect(rect, wordPaint);


        //重置rect   並把文字置於rect中間
        rect = new Rect(rectLeft, rectTop - 80, rectRight, rectTop - 10);
        wordPaint.setColor(Color.WHITE);
        String[] qrArray;
        qrArray = mtext.split("/");
        canvas.drawText(TIPS + qrArray[15], rect.centerX(), baseline, wordPaint);

        canvas.drawLine(rectLeft, rectTop, rectLeft + lineLen, rectTop, linePaint);
        canvas.drawLine(rectRight - lineLen, rectTop, rectRight, rectTop, linePaint);
        canvas.drawLine(rectLeft, rectTop, rectLeft, rectTop + lineLen, linePaint);
        canvas.drawLine(rectRight, rectTop, rectRight, rectTop + lineLen, linePaint);
        canvas.drawLine(rectLeft, rectBottom, rectLeft + lineLen, rectBottom, linePaint);
        canvas.drawLine(rectRight - lineLen, rectBottom, rectRight, rectBottom, linePaint);
        canvas.drawLine(rectLeft, rectBottom - lineLen, rectLeft, rectBottom, linePaint);
        canvas.drawLine(rectRight, rectBottom - lineLen, rectRight, rectBottom, linePaint);

        linePaint.setStrokeWidth(10);
        //canvas.drawRect((rectLeft+rectRight)/2 - 10,(rectTop+rectBottom)/2 - 10,(rectLeft+rectRight)/2 + 10,(rectTop+rectBottom)/2 + 10,linePaint);
        //canvas.drawCircle((rectLeft+rectRight)/2,(rectTop+rectBottom)/2,10,linePaint);

        //補正
        float err = (float) 1;

        //遮罩範圍
        Paint rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Style.STROKE);
        rectPaint.setStrokeWidth(LINE_WIDTH);// 線寬
        rectPaint.setColor(Color.rgb(get_color(channel_color_code).get(0), get_color(channel_color_code).get(1), get_color(channel_color_code).get(2)));
        canvas.drawRect(rectLeft + (channelRect.x * err),
                rectTop + (channelRect.y * err),
                rectLeft + ((channelRect.x * err) + channelRect.width),
                rectTop + ((channelRect.y * err) + channelRect.height),
                rectPaint);

//        canvas.drawRect(rectLeft + (432),
//                rectTop + (550),
//                rectLeft + (432 + 136),
//                rectTop + (550 + 200),
//                rectPaint);

        rectPaint.setColor(Color.rgb(get_color(digi_color_code).get(0), get_color(digi_color_code).get(1), get_color(digi_color_code).get(2)));
        canvas.drawRect(rectLeft + digiRect.x,
                rectTop + digiRect.y,
                rectLeft + (digiRect.x + digiRect.width),
                rectTop + (digiRect.y + digiRect.height),
                rectPaint);

//        canvas.drawRect(rectLeft + 10,
//                rectTop + 292,
//                rectLeft + (10 + 475),
//                rectTop + (292 + 200),
//                rectPaint);

    }

    public int getRectLeft() {
        return rectLeft;
    }

    public int getRectTop() {
        return rectTop;
    }

    public int getRectRight() {
        return rectRight;
    }

    public int getRectBottom() {
        return rectBottom;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public List<Integer> get_color(int i) {
        List color;
        if (i == 1) { //紅色
            color = Arrays.asList(0xff, 0x24, 0x50);
        }
        else if (i == 2) { //綠色
            color = Arrays.asList(176, 221, 118);
        }
        else if (i == 3) { //藍色
            color = Arrays.asList(106, 179, 239);
        }
        else {
            color = Arrays.asList(249, 115, 163);
        }
        return color;
    }

}
