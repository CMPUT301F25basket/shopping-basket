package com.example.shopping_basket;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.nfc.FormatException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

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
        editText.setFocusable(false);
        editText.setClickable(true);

        editText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            // Try to parse existing date to pre-fill the picker
            Date existingDate = stringToDate(editText.getText().toString(), "MM/dd/yyyy");
            if (existingDate != null) {
                calendar.setTime(existingDate);
            }

            DatePickerDialog datePicker = new DatePickerDialog(context,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);
                        editText.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Set min date if needed, but for filters it might be better to allow past dates
            // datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
            datePicker.show();
        });

        addClearButtonFunctionality(context, editText);
    }

    /**
     * Displays a {@link TimePickerDialog} pre-filled with the current time in 24-hour format.
     * Upon selection, the provided {@link EditText} is updated with the formatted time string "HH:mm".
     *
     * @param context  The {@link Context} required to show the dialog (e.g., an Activity).
     * @param editText The {@link EditText} widget that will display the selected time.
     */
    public static void showTimePicker(Context context, EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);

        editText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            Date existingTime = stringToDate(editText.getText().toString(), "HH:mm");
            if (existingTime != null) {
                calendar.setTime(existingTime);
            }

            TimePickerDialog timePicker = new TimePickerDialog(
                    context,
                    (view, selectedHour, selectedMinute) -> {
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        editText.setText(selectedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // 24-hour format
            );
            timePicker.show();
        });
    }

        /**
         * Displays a DatePickerDialog followed by a TimePickerDialog to select a full date and time.
         * <p>
         * Upon completion, the provided EditText is updated with the combined, formatted
         * date and time string "MM/dd/yyyy HH:mm".
         *
         * @param context  The Context required to show the dialogs.
         * @param editText The EditText widget that will display the selected date and time.
         */
        public static void showDateTimePicker(Context context, EditText editText) {
            editText.setFocusable(false);
            editText.setClickable(true);

            editText.setOnClickListener(v -> {
                final Calendar calendar = Calendar.getInstance();
                Date existingDateTime = stringToDate(editText.getText().toString(), "MM/dd/yyyy HH:mm");
                if (existingDateTime != null) {
                    calendar.setTime(existingDateTime);
                }

                DatePickerDialog datePicker = new DatePickerDialog(context,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            calendar.set(Calendar.YEAR, selectedYear);
                            calendar.set(Calendar.MONTH, selectedMonth);
                            calendar.set(Calendar.DATE, selectedDay);

                            // Setup time picker after a date has been selected
                            TimePickerDialog timePicker = new TimePickerDialog(context,
                                    (timeView, selectedHour, selectedMinute) -> {
                                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                        calendar.set(Calendar.MINUTE, selectedMinute);

                                        String selectedDateTime = dateFormatter(calendar.getTime(), "MM/dd/yyyy HH:mm");
                                        editText.setText(selectedDateTime);
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                            );
                            timePicker.show();
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE)
                );
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                datePicker.show();
            });

            addClearButtonFunctionality(context, editText);
        }

    /**
     * Adds the clear button functionality (text watcher and touch listener) to an EditText.
     * This is a private helper to avoid code duplication across the public methods.
     */
    @SuppressLint("ClickableViewAccessibility")
    private static void addClearButtonFunctionality(Context context, EditText editText) {
        // Add a listener to show/hide the clear icon based on text content
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateClearIcon(context, editText, !s.toString().isEmpty());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add a listener to handle clicks on the clear icon itself
        editText.setOnTouchListener((v, event) -> {
            // Check if the touch event is on the clear icon (the drawableEnd)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (editText.getCompoundDrawables()[2] != null) { // Index 2 is for drawableEnd
                    // Check if the click was within the bounds of the clear icon
                    boolean isClearButtonClicked = event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width() - editText.getPaddingRight());
                    if (isClearButtonClicked) {
                        editText.setText(""); // Clear the text
                        return true; // Consume the touch event
                    }
                }
            }
            // Let the default OnTouch behavior (and subsequently OnClick) handle other touches
            return v.onTouchEvent(event);
        });

        // Initial check to set the icon state correctly when the view is first created
        updateClearIcon(context, editText, !editText.getText().toString().isEmpty());
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
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
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
     * Formats a {@link Date} object into a string based on the provided format.
     *
     * @param d      The Date to format.
     * @param format The date format pattern to use (e.g., "MM/dd/yyyy", "HH:mm").
     * @return The formatted date string, or null if formatting fails due to an invalid format.
     */
    public static String dateFormatter(Date d, String format) {
        if (d == null || format == null) {
            Log.e(TAG, "Date or format string is null.");
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            return sdf.format(d.getTime());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid format provided to dateFormatter: "+ e);
            return null;
        }
    }

    /**
     * Shows or hides the 'clear' icon at the end of the EditText.
     * @param editText The EditText to modify.
     * @param show     True to show the icon, false to hide it.
     */
    private static void updateClearIcon(Context context, EditText editText, boolean show) {
        if (show) {
            android.graphics.drawable.Drawable clearIcon = ContextCompat.getDrawable(context, R.drawable.eraser_svgrepo_com);
            if (clearIcon != null) {
                int size = (int) (editText.getTextSize() * 1.2);

                clearIcon.setBounds(0, 0, size, size);
            }
            editText.setCompoundDrawables(null, null, clearIcon, null);
        } else {
            // If not showing, remove all drawables.
            editText.setCompoundDrawables(null, null, null, null);
        }
    }
}
