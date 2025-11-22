package com.example.shopping_basket;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A utility class providing helper methods for handling date and time operations, including static methods to:
 * - Display a {@link DatePickerDialog} and fill an {@link EditText} with the selected date.
 * - Display a {@link TimePickerDialog} and fill an {@link EditText} with the selected time.
 * - Convert a date String into a Date object.
 */
public class CalendarUtils {
    /**
     * Displays a {@link DatePickerDialog} pre-filled with the current date.
     * Upon selection, the provided {@link EditText} is updated with the formatted date string "MM/dd/yyyy".
     *
     * @param context  The {@link Context} required to show the dialog (e.g., an Activity).
     * @param editText The {@link EditText} widget that will display the selected date.
     */
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

    /**
     * Displays a {@link TimePickerDialog} pre-filled with the current time in 24-hour format.
     * Upon selection, the provided {@link EditText} is updated with the formatted time string "HH:mm".
     *
     * @param context  The {@link Context} required to show the dialog (e.g., an Activity).
     * @param editText The {@link EditText} widget that will display the selected time.
     */
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

    /**
     * Converts a String representation of a date into a Date object
     * and handles parsing errors by logging them and returning null.
     *
     * @param dateString The string to be parsed (e.g., "11/15/2025").
     * @param format     The date format pattern of the input string (e.g., "MM/dd/yyyy").
     * @return A Date object representing the parsed string, or null if parsing fails.
     */
    public static Date stringToDate(String dateString, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            // Log the error for debugging purposes
            Log.e(TAG, "Failed to parse date string: " + dateString + " with format: " + format, e);
            return null;
        }
    }

    /**
     * Formats a {@link Date} object into a "MM/dd/yyyy" string.
     * @param d The Date to format.
     * @return The formatted date string.
     */
    public static String dateFormatter(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return sdf.format(d.getTime());
    }
}
