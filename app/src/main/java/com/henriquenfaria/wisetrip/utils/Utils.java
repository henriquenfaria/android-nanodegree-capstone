package com.henriquenfaria.wisetrip.utils;

import com.henriquenfaria.wisetrip.models.Traveler;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

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
}
