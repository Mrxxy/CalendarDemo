package com.stockholm.library.calendar.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;

import com.stockholm.library.R;
import com.stockholm.library.calendar.CalendarUtils;
import com.stockholm.library.calendar.LunarCalendarUtils;

import java.util.Calendar;

public class MonthView extends View {
    private static final String TAG = MonthView.class.getSimpleName();
    private static final int RESTDAY = 1;
    private static final int WORKDAY = 2;

    private static final int NUM_COLUMNS = 7;
    private Paint paint;
    private Paint lunarPaint;
    private int colorNormalDay;
    private int colorSelectDay;
    private int colorSelectBG;
    private int colorSelectBGToday;
    private int colorCurrentDay;
    private int colorLunarText;
    private int colorHolidayText;
    private int colorLastOrNextMonthText;
    private int colorLastOrNextMonthHoliday;
    private int currentYear, currentMonth, currentDay;
    private int selYear, selMonth, selDay;
    private int columnSize, rowSize, selectCircleSize;
    private int dayTextSize;
    private int lunarTextSize;
    private int weekRow; // 当前月份第几周
    private int[][] daysTextArray;
    private int[] holidaysArray;
    private String[][] holidayOrLunarTextArray;
    private boolean isShowLunar;
    private boolean isShowHolidayHint;
    private DisplayMetrics displayMetrics;
    private OnMonthClickListener dateClickListener;
    private GestureDetector mGestureDetector;
    private Bitmap restBitmap, restBitmapGray, workBitmap, workBitmapGray;

    private int middleGap;//3-4行间距


    public MonthView(Context context, int year, int month) {
        this(context, null, year, month);
    }

    public MonthView(Context context, TypedArray array, int year, int month) {
        this(context, array, null, year, month);
    }

    public MonthView(Context context, TypedArray array, AttributeSet attrs, int year, int month) {
        this(context, array, attrs, 0, year, month);
    }

    public MonthView(Context context, TypedArray array, AttributeSet attrs, int defStyleAttr, int year, int month) {
        super(context, attrs, defStyleAttr);
        initAttrs(array, year, month);
        initPaint();
        initMonth();
    }

    private void initAttrs(TypedArray array, int year, int month) {
        if (array != null) {
            colorSelectDay = array.getColor(R.styleable.MonthCalendarView_month_selected_text_color, Color.parseColor("#FFFFFF"));
            colorSelectBG = array.getColor(R.styleable.MonthCalendarView_month_selected_circle_color, Color.parseColor("#E8E8E8"));
            colorSelectBGToday = array.getColor(R.styleable.MonthCalendarView_month_selected_circle_today_color, Color.parseColor("#F43C3C"));
            colorNormalDay = array.getColor(R.styleable.MonthCalendarView_month_normal_text_color, Color.parseColor("#4D4D4D"));
            colorCurrentDay = array.getColor(R.styleable.MonthCalendarView_month_today_text_color, Color.parseColor("#F5B8B8"));
            colorLastOrNextMonthText = array.getColor(R.styleable.MonthCalendarView_month_last_or_next_month_text_color, Color.parseColor("#999999"));
            colorLastOrNextMonthHoliday = array.getColor(R.styleable.MonthCalendarView_month_last_or_next_month_text_color, Color.parseColor("#F5B8B8"));
            colorLunarText = array.getColor(R.styleable.MonthCalendarView_month_lunar_text_color, Color.parseColor("#ACA9BC"));
            colorHolidayText = array.getColor(R.styleable.MonthCalendarView_month_holiday_color, Color.parseColor("#F5B8B8"));
            dayTextSize = array.getInteger(R.styleable.MonthCalendarView_month_day_text_size, 13);
            lunarTextSize = array.getInteger(R.styleable.MonthCalendarView_month_day_lunar_text_size, 8);
            isShowLunar = array.getBoolean(R.styleable.MonthCalendarView_month_show_lunar, true);
            isShowHolidayHint = array.getBoolean(R.styleable.MonthCalendarView_month_show_holiday_hint, true);
        } else {
            colorSelectDay = Color.parseColor("#FFFFFF");
            colorSelectBG = Color.parseColor("#E8E8E8");
            colorSelectBGToday = Color.parseColor("#F43C3C");
            colorNormalDay = Color.parseColor("#4D4D4D");
            colorCurrentDay = Color.parseColor("#F5B8B8");
            colorLastOrNextMonthText = Color.parseColor("#999999");
            colorLastOrNextMonthHoliday = Color.parseColor("#F5B8B8");
            colorHolidayText = Color.parseColor("#F5B8B8");
            dayTextSize = 13;
            lunarTextSize = 8;
            isShowLunar = true;
            isShowHolidayHint = true;
        }
        selYear = year;
        selMonth = month;
        restBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rest_day);
        restBitmapGray = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rest_day_gray);
        workBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_work_day);
        workBitmapGray = BitmapFactory.decodeResource(getResources(), R.drawable.ic_work_day_gray);
        holidaysArray = CalendarUtils.getInstance(getContext()).getHolidays(selYear, selMonth + 1);
    }

    private void initPaint() {
        displayMetrics = getResources().getDisplayMetrics();
        middleGap = (int) (50 * displayMetrics.scaledDensity);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(dayTextSize * displayMetrics.scaledDensity);

        lunarPaint = new Paint();
        lunarPaint.setAntiAlias(true);
        lunarPaint.setTextSize(lunarTextSize * displayMetrics.scaledDensity);
        lunarPaint.setColor(colorLunarText);
    }

    private void initMonth() {
        Calendar calendar = Calendar.getInstance();
        boolean monthRowsIsSix = CalendarUtils.getMonthRows(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)) == 6;
        Log.d(TAG, "initMonth: " + monthRowsIsSix);
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDay = calendar.get(Calendar.DATE);
        if (selYear == currentYear && selMonth == currentMonth) {
            setSelectYearMonth(selYear, selMonth, currentDay);
        } else {
            setSelectYearMonth(selYear, selMonth, 1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = displayMetrics.densityDpi * 420;
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = displayMetrics.densityDpi * 420;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initSize();
        clearData();
        drawLastMonth(canvas);
        drawThisMonth(canvas);
        drawNextMonth(canvas);
        drawLunarText(canvas);
        drawHoliday(canvas);
    }

    private void initSize() {
        columnSize = getWidth() / NUM_COLUMNS;
        rowSize = (getHeight() - middleGap) / 6;
        selectCircleSize = (int) (20 * displayMetrics.scaledDensity);
        while (selectCircleSize > rowSize / 2) {
            selectCircleSize = (int) (selectCircleSize / 1.3);
        }
    }

    private void clearData() {
        daysTextArray = new int[6][7];
        holidayOrLunarTextArray = new String[6][7];
    }

    private void drawLastMonth(Canvas canvas) {
        int lastYear, lastMonth;
        if (selMonth == 0) {
            lastYear = selYear - 1;
            lastMonth = 11;
        } else {
            lastYear = selYear;
            lastMonth = selMonth - 1;
        }
        int monthDays = CalendarUtils.getMonthDays(lastYear, lastMonth);
        int weekNumber = CalendarUtils.getFirstDayWeek(selYear, selMonth);
        int lastMonthDays = weekNumber - 1;
        for (int day = 0; day < lastMonthDays; day++) {
            if (isWeekend(day)) {
                paint.setColor(colorLastOrNextMonthHoliday);
            } else {
                paint.setColor(colorLastOrNextMonthText);
            }
            daysTextArray[0][day] = monthDays - weekNumber + day + 2;
            String dayString = String.valueOf(daysTextArray[0][day]);
            int startX = (int) (columnSize * day + (columnSize - paint.measureText(dayString)) / 2);
            int startY = (int) (rowSize / 2 - (paint.ascent() + paint.descent()) / 2);
            canvas.drawText(dayString, startX, startY, paint);
            holidayOrLunarTextArray[0][day] = CalendarUtils.getHolidayFromSolar(lastYear, lastMonth, daysTextArray[0][day]);
        }
    }

    private void drawThisMonth(Canvas canvas) {
        String dayString;
        int monthDays = CalendarUtils.getMonthDays(selYear, selMonth);
        int weekNumber = CalendarUtils.getFirstDayWeek(selYear, selMonth);
        for (int day = 0; day < monthDays; day++) {
            dayString = String.valueOf(day + 1);
            int col = (day + weekNumber - 1) % 7;
            int row = (day + weekNumber - 1) / 7;
            daysTextArray[row][col] = day + 1;
            int startX = (int) (columnSize * col + (columnSize - paint.measureText(dayString)) / 2);
            int startY = (int) (rowSize * row + rowSize / 2 - (paint.ascent() + paint.descent()) / 2);
            if (row >= 3) {
                startY = (int) (startY + 50 * displayMetrics.scaledDensity);
            }
            // 绘制当前选中圆环
            if (dayString.equals(String.valueOf(selDay))) {
                int startRecX = columnSize * col;
                int startRecY = rowSize * row;
                if (row >= 3) startRecY += middleGap;
                int endRecX = startRecX + columnSize;
                int endRecY = startRecY + rowSize;
                if (selYear == currentYear && currentMonth == selMonth && day + 1 == currentDay) {
                    paint.setColor(colorSelectBGToday);
                } else {
                    paint.setColor(colorSelectBG);
                }
                canvas.drawCircle((startRecX + endRecX) / 2, (startRecY + endRecY) / 2, selectCircleSize, paint);
                weekRow = row + 1;
            }
            if (isWeekend(col)) {
                paint.setColor(colorHolidayText);
            } else {
                paint.setColor(colorNormalDay);
            }
            if (dayString.equals(String.valueOf(selDay))) {
                paint.setColor(colorSelectDay);
            } else if (dayString.equals(String.valueOf(currentDay)) && currentDay != selDay && currentMonth == selMonth && currentYear == selYear) {
                paint.setColor(colorCurrentDay);
            }
            canvas.drawText(dayString, startX, startY, paint);
            holidayOrLunarTextArray[row][col] = CalendarUtils.getHolidayFromSolar(selYear, selMonth, daysTextArray[row][col]);
        }
    }

    private void drawNextMonth(Canvas canvas) {
        int monthDays = CalendarUtils.getMonthDays(selYear, selMonth);
        int weekNumber = CalendarUtils.getFirstDayWeek(selYear, selMonth);
        int nextMonthDays = 42 - monthDays - weekNumber + 1;
        int nextMonth = selMonth + 1;
        int nextYear = selYear;
        if (nextMonth == 12) {
            nextMonth = 0;
            nextYear += 1;
        }
        for (int day = 0; day < nextMonthDays; day++) {
            int column = (monthDays + weekNumber - 1 + day) % 7;
            int row = 5 - (nextMonthDays - day - 1) / 7;
            paint.setColor(colorLastOrNextMonthText);
            if (isWeekend(column)) {
                paint.setColor(colorLastOrNextMonthHoliday);
            }
            try {
                daysTextArray[row][column] = day + 1;
                holidayOrLunarTextArray[row][column] = CalendarUtils.getHolidayFromSolar(nextYear, nextMonth, daysTextArray[row][column]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String dayString = String.valueOf(daysTextArray[row][column]);
            int startX = (int) (columnSize * column + (columnSize - paint.measureText(dayString)) / 2);
            int startY = (int) (rowSize * row + rowSize / 2 - (paint.ascent() + paint.descent()) / 2) + middleGap;
            canvas.drawText(dayString, startX, startY, paint);
        }
    }

    private void drawLunarText(Canvas canvas) {
        if (isShowLunar) {
            int firstYear, firstMonth, firstDay;
            int weekNumber = CalendarUtils.getFirstDayWeek(selYear, selMonth);
            if (weekNumber == 1) {
                firstYear = selYear;
                firstMonth = selMonth + 1;
                firstDay = 1;
            } else {
                int monthDays;
                if (selMonth == 0) {
                    firstYear = selYear - 1;
                    firstMonth = 11;
                    monthDays = CalendarUtils.getMonthDays(firstYear, firstMonth);
                    firstMonth = 12;
                } else {
                    firstYear = selYear;
                    firstMonth = selMonth - 1;
                    monthDays = CalendarUtils.getMonthDays(firstYear, firstMonth);
                    firstMonth = selMonth;
                }
                firstDay = monthDays - weekNumber + 2;
            }
            LunarCalendarUtils.Lunar lunar = LunarCalendarUtils.solarToLunar(new LunarCalendarUtils.Solar(firstYear, firstMonth, firstDay));
            int days;
            int day = lunar.lunarDay;
            int leapMonth = LunarCalendarUtils.leapMonth(lunar.lunarYear);
            days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
            for (int i = 0; i < 42; i++) {
                int column = i % 7;
                int row = i / 7;
                if (day > days) {
                    day = 1;
                    if (lunar.lunarMonth == 12) {
                        lunar.lunarMonth = 1;
                        lunar.lunarYear = lunar.lunarYear + 1;
                    }
                    if (lunar.lunarMonth == leapMonth) {
                        days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
                    } else {
                        lunar.lunarMonth++;
                        days = LunarCalendarUtils.daysInLunarMonth(lunar.lunarYear, lunar.lunarMonth);
                    }
                }
                String dayString = holidayOrLunarTextArray[row][column];
                //last or next month
                if (isLastOrNextMonth(row, column)) {
                    lunarPaint.setColor(colorLastOrNextMonthHoliday);
                    if ("".equals(dayString)) {
                        dayString = LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day);
                    }
                    if ("".equals(dayString)) {
                        dayString = LunarCalendarUtils.getLunarDayString(day);
                        lunarPaint.setColor(colorLastOrNextMonthText);
                    }
                } else {
                    // this month
                    lunarPaint.setColor(colorHolidayText);
                    if ("".equals(dayString)) {
                        dayString = LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day);
                    }
                    if ("".equals(dayString)) {
                        dayString = LunarCalendarUtils.getLunarDayString(day);
                        lunarPaint.setColor(colorNormalDay);
                    }
                }
                int startX = (int) (columnSize * column + (columnSize - lunarPaint.measureText(dayString)) / 2);
                int startY = (int) (rowSize * row + rowSize * 0.96f - (lunarPaint.ascent() + lunarPaint.descent()) / 2);
                if (row >= 3) {
                    startY += middleGap;
                }
                canvas.drawText(dayString, startX, startY, lunarPaint);
                day++;
            }
        }
    }

    /**
     * 绘制节日图标
     *
     * @param canvas
     */
    private void drawHoliday(Canvas canvas) {
        if (isShowHolidayHint) {
            Rect rect = new Rect(0, 0, restBitmap.getWidth(), restBitmap.getHeight());
            Rect rectF = new Rect();
            int distance = (int) (selectCircleSize / 3.4f);
            for (int i = 0; i < holidaysArray.length; i++) {
                int column = i % 7;
                int row = i / 7;
                int left = columnSize * (column + 1) - restBitmap.getWidth() - distance;
                int top = rowSize * row + distance;
                int right = columnSize * (column + 1) - distance;
                int bottom = rowSize * row + restBitmap.getHeight() + distance;
                if (row >= 3) {
                    top += middleGap;
                    bottom += middleGap;
                }
                rectF.set(left, top, right, bottom);
                if (isLastOrNextMonth(row, column)) {
                    if (holidaysArray[i] == RESTDAY) {
                        canvas.drawBitmap(restBitmapGray, rect, rectF, null);
                    } else if (holidaysArray[i] == WORKDAY) {
                        canvas.drawBitmap(workBitmapGray, rect, rectF, null);
                    }
                } else {
                    if (holidaysArray[i] == RESTDAY) {
                        canvas.drawBitmap(restBitmap, rect, rectF, null);
                    } else if (holidaysArray[i] == WORKDAY) {
                        canvas.drawBitmap(workBitmap, rect, rectF, null);
                    }
                }
            }
        }
    }

    private boolean isWeekend(int column) {
        return column == 0 || column == 6;
    }

    private boolean isLastOrNextMonth(int row, int column) {
        return row == 0 && daysTextArray[row][column] >= 23 || row >= 4 && daysTextArray[row][column] <= 14;
    }

    public void setSelectYearMonth(int year, int month, int day) {
        selYear = year;
        selMonth = month;
        selDay = day;
    }

    /**
     * 跳转到某日期
     *
     * @param year
     * @param month
     * @param day
     */
    public void clickThisMonth(int year, int month, int day) {
        if (dateClickListener != null) {
            dateClickListener.onClickThisMonth(year, month, day);
        }
        setSelectYearMonth(year, month, day);
        invalidate();
    }

    /**
     * 获取当前选择年
     *
     * @return
     */
    public int getSelectYear() {
        return selYear;
    }

    /**
     * 获取当前选择月
     *
     * @return
     */
    public int getSelectMonth() {
        return selMonth;
    }

    /**
     * 获取当前选择日
     *
     * @return
     */
    public int getSelectDay() {
        return this.selDay;
    }

    public int getRowSize() {
        return rowSize;
    }

    public int getWeekRow() {
        return weekRow;
    }

    /**
     * 设置点击日期监听
     *
     * @param dateClickListener
     */
    public void setOnDateClickListener(OnMonthClickListener dateClickListener) {
        this.dateClickListener = dateClickListener;
    }

}

