package com.stockholm.library.calendar;


public interface OnCalendarClickListener {

    void onClickDate(int year, int month, int day);

    void onPageChange(int year, int month, int day);
}