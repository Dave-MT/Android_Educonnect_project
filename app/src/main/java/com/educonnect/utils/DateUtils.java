package com.educonnect.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat SERVER_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);

    public static String formatDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return "N/A";
        }

        try {
            Date date = SERVER_FORMAT.parse(dateTimeString);
            return DISPLAY_FORMAT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeString;
        }
    }

    public static String getCurrentDateTime() {
        return SERVER_FORMAT.format(new Date());
    }

    public static String formatForServer(Date date) {
        return SERVER_FORMAT.format(date);
    }

    public static String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "N/A";
        }

        try {
            // Try parsing with different formats
            SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

            Date date;
            try {
                date = inputFormat1.parse(dateString);
            } catch (ParseException e) {
                date = inputFormat2.parse(dateString);
            }

            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }
}
