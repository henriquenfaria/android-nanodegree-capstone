package com.henriquenfaria.wisetrip.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import com.henriquenfaria.wisetrip.models.Traveler;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;


/* Utility class with static helper methods */
public class Utils {

    public static String getFormattedFullTripDateText(long startDateMillis, long endDateMillis) {
        Calendar startDateCalendar = Calendar.getInstance(Locale.getDefault());
        startDateCalendar.setTimeInMillis(startDateMillis);
        Calendar endDateCalendar = Calendar.getInstance(Locale.getDefault());
        endDateCalendar.setTimeInMillis(endDateMillis);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

        return dateFormat.format(startDateCalendar.getTime()) +
                " ~ " +
                dateFormat.format(endDateCalendar.getTime());
    }

    public static String getFormattedTripDateText(long dateMillis) {
        Calendar dateCalendar = Calendar.getInstance(Locale.getDefault());
        dateCalendar.setTimeInMillis(dateMillis);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        return dateFormat.format(dateCalendar.getTime());
    }

    public static String getFormattedExpenseDateText(long dateMillis) {
        Calendar startDateCalendar = Calendar.getInstance(Locale.getDefault());
        startDateCalendar.setTimeInMillis(dateMillis);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        return dateFormat.format(startDateCalendar.getTime());
    }


    public static String getFormattedTravelersText(Map<String, Traveler> travelers) {
        StringBuilder travelersString = new StringBuilder();
        int count_for_comma = 0;
        for (Map.Entry entry : travelers.entrySet()) {
            Traveler traveler = (Traveler) entry.getValue();
            travelersString.append(traveler.getName());
            if (++count_for_comma < travelers.size()) {
                travelersString.append(", ");
            }
        }

        return travelersString.toString();
    }

    public static void saveBitmapToInternalStorage(Context context, Bitmap bitmapImage, String
            directory, String fileName) {
        FileOutputStream fos = null;
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File photoFile = new File(directoryFile, fileName);
        try {
            fos = new FileOutputStream(photoFile);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            Timber.e("IOException while saving bitmap to internal storage");
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Timber.e("IOException while saving bitmap to internal storage");
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getBitmapFromInternalStorage(Context context, String directory, String
            fileName) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File photoFile = new File(directoryFile, fileName);

        if (photoFile.exists()) {
            return BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        }

        return null;
    }

    public static boolean deleteFileFromInternalStorage(Context context, String directory,
                                                        String fileName, boolean
                                                                clearPicassoCache) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File file = new File(directoryFile, fileName);
        boolean isDeleted = false;

        try {
            isDeleted = file.delete();
            if (isDeleted && clearPicassoCache) {
                Picasso.with(context).invalidate(file);
            }
        } catch (SecurityException e) {
            Timber.e("SecurityException while deleting bitmap from internal storage");
            e.printStackTrace();
        }

        return isDeleted;
    }

    public static boolean deleteFolderFromInternalStorage(Context context, String directory,
                                                          boolean clearPicassoCache) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        if (directoryFile.isDirectory()) {
            for (File childFiled : directoryFile.listFiles()) {
                boolean isDeleted = childFiled.delete();
                if (isDeleted && clearPicassoCache) {
                    Picasso.with(context).invalidate(childFiled);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFileExists(Context context, String directory, String fileName) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File file = new File(directoryFile, fileName);
        return file.exists();
    }

    public static void saveBooleanToSharedPrefs(Context context, String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBooleanFromSharedPrefs(Context context, String key,
                                                    boolean defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(key, defaultValue);
    }

    public static void saveStringToSharedPrefs(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringFromSharedPrefs(Context context, String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(key, "");
    }

    // Based on https://stackoverflow.com/a/24397810/2983102
    public static String getRestrictedDecimal(String str, int max_digits_before_point,
                                              int max_decimal_digits) {
        if (str.charAt(0) == '.') str = "0" + str;
        int max = str.length();

        String rFinal = "";
        boolean after = false;
        int i = 0, up = 0, decimal = 0;
        char t;
        while (i < max) {
            t = str.charAt(i);
            if (t != '.' && after == false) {
                up++;
                if (up > max_digits_before_point) return rFinal;
            } else if (t == '.') {
                after = true;
            } else {
                decimal++;
                if (decimal > max_decimal_digits)
                    return rFinal;
            }
            rFinal = rFinal + t;
            i++;
        }
        return rFinal;
    }

    public static String getCurrencySymbol(String countryCode) {
        String currencySymbol = "";
        Locale locale = null;
        Currency currency = null;
        try {
            locale = new Locale("", countryCode);
            currency = Currency.getInstance(locale);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (currency != null) {
            currencySymbol = currency.getCurrencyCode();
        }

        return currencySymbol;
    }
}
