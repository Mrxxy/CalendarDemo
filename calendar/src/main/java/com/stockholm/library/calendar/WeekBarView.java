package com.stockholm.library.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.stockholm.library.R;


public class WeekBarView extends View {

    private int workdayTextColor;
    private int weekendTextColor;
    private int weekSize;
    private Paint paint;
    private DisplayMetrics displayMetrics;
    private String[] weekString;

    public WeekBarView(Context context) {
        this(context, null);
    }

    public WeekBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WeekBarView);
        workdayTextColor = array.getColor(R.styleable.WeekBarView_week_text_color, Color.parseColor("#4D4D4D"));
        weekendTextColor = array.getColor(R.styleable.WeekBarView_week_text_color_weekend, Color.parseColor("#F43C3C"));
        weekSize = array.getInteger(R.styleable.WeekBarView_week_text_size, 13);
        weekString = context.getResources().getStringArray(R.array.calendar_week);
        array.recycle();
    }

    private void initPaint() {
        displayMetrics = getResources().getDisplayMetrics();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(weekSize * displayMetrics.scaledDensity);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = displayMetrics.densityDpi * 30;
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = displayMetrics.densityDpi * 300;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int columnWidth = width / 7;
        for (int i = 0; i < weekString.length; i++) {
            paint.setColor(workdayTextColor);
            String text = weekString[i];
            int fontWidth = (int) paint.measureText(text);
            int startX = columnWidth * i + (columnWidth - fontWidth) / 2;
            int startY = (int) (height / 2 - (paint.ascent() + paint.descent()) / 2);
            if (i == 0 || i == weekString.length - 1) {
                paint.setColor(weekendTextColor);
            }
            canvas.drawText(text, startX, startY, paint);
        }
    }

}
