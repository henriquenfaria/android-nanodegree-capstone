package com.henriquenfaria.wisetrip.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import com.henriquenfaria.wisetrip.models.Traveler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
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

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(dateFormat.format(startDateCalendar.getTime()));
        stringBuffer.append(" ~ ");
        stringBuffer.append(dateFormat.format(endDateCalendar.getTime()));

        return stringBuffer.toString();
    }

    public static String getFormattedTripDateText(long dateMillis) {
        Calendar dateCalendar = Calendar.getInstance(Locale.getDefault());
        dateCalendar.setTimeInMillis(dateMillis);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        return dateFormat.format(dateCalendar.getTime());
    }


    public static String getFormattedTravelersText(Map<String, Traveler> travelers) {
        StringBuffer travelersString = new StringBuffer();
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
            directory,
                                                   String fileName) {
        FileOutputStream fos = null;
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File photoFile = new File(directoryFile, fileName);
        try {
            fos = new FileOutputStream(photoFile);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
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

    public static boolean deleteFileFromInternalStorage(Context context, String directory, String
            fileName) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File file = new File(directoryFile, fileName);
        boolean isDeleted = false;


        try {
            isDeleted = file.delete();
        } catch (SecurityException e) {
            Timber.e("SecurityException while deleting bitmap from internal storage");
            e.printStackTrace();
        }

        return isDeleted;
    }

    public static boolean deleteFolderFromInternalStorage(Context context, String directory) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        if (directoryFile.isDirectory()) {
            for (File child : directoryFile.listFiles()) {
                child.delete();
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
        editor.commit();
    }

    public static boolean getBooleanFromSharedPrefs(Context context, String key,
                                                    boolean defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(key, defaultValue);
    }
}
