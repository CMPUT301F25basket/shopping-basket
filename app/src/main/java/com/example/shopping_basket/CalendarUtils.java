package com.example.shopping_basket;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarUtils {
    public static void showDatePicker(Context context, EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);
                    editText.setText(selectedDate);
                },
                year, month, day
        );

        datePicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePicker.show();
    }

    public static void showTimePicker(Context context, EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(
                context,
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    editText.setText(selectedTime);
                },
                hour, minute, true
        );

        timePicker.show();
    }
    public static Date stringToDate(String dateString, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            // This directly returns the Date object after parsing.
            return sdf.parse(dateString);
        } catch (ParseException e) {
            // It's good practice to log the error to see what went wrong.
            e.printStackTrace();
            return null;
        }
    }
}
