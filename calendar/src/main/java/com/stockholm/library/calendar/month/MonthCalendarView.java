package com.stockholm.library.calendar.month;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.stockholm.library.R;
import com.stockholm.library.calendar.OnCalendarClickListener;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Calendar;
import java.util.TimeZone;


public class MonthCalendarView extends LinearLayout implements OnMonthClickListener {

    private Context context;
    private OnCalendarClickListener mOnCalendarClickListener;
    private MonthView monthDateView;
    private TimeReceiver timeReceiver;

    public MonthCalendarView(Context context) {
        this(context, null);
    }

    public MonthCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        initMonthAdapter(context, context.obtainStyledAttributes(attrs, R.styleable.MonthCalendarView));
    }

    private void initMonthAdapter(Context context, TypedArray array) {
        LocalDate date = LocalDate.now(DateTimeZone.forTimeZone(TimeZone.getDefault()));
        monthDateView = new MonthView(context, array, date.getYear(), date.getMonthOfYear() - 1);
        monthDateView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        monthDateView.invalidate();
        monthDateView.setOnDateClickListener(this);
        addView(monthDateView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        timeReceiver = new TimeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        context.registerReceiver(timeReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        context.unregisterReceiver(timeReceiver);
    }

    @Override
    public void onClickThisMonth(int year, int month, int day) {
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(year, month, day);
        }
    }

    @Override
    public void onClickLastMonth(int year, int month, int day) {
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day);
        }
    }

    @Override
    public void onClickNextMonth(int year, int month, int day) {
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day);
            monthDateView.invalidate();
        }
        onClickThisMonth(year, month, day);
    }

    /**
     * 跳转到今天
     */
    public void setTodayToView() {
        if (monthDateView != null) {
            Calendar calendar = Calendar.getInstance();
            monthDateView.clickThisMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        }
    }

    /**
     * 设置点击日期监听
     *
     * @param onCalendarClickListener
     */
    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }

    private class TimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LocalTime now = LocalTime.now();
            if ((now.getHourOfDay() == 0 && now.getMinuteOfHour() == 0)
                    || action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)) {
                monthDateView.postInvalidate();
            }
        }
    }

}