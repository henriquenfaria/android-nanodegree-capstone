package com.henriquenfaria.wisetrip.utils;


// Class that holds all app's public constants
public class Constants {
    // General constants
    public static class General {
        // Max TripModel destinations
        public static final int MAX_DESTINATIONS = 10;
        // Max TripModel travelers
        public static final int MAX_TRAVELERS = 10;
        // Internal Storage directory for destination photos
        public static final String DESTINATION_PHOTO_DIR = "destinations";
        // 24 hours in milliseconds
        public static final long DAY_IN_MILLIS = 86400000L;

        // Constants for money amount edit texts
        public static final int MAX_DIGITS_BEFORE_POINT = 12;
        public static final int MAX_DECIMAL_DIGITS = 2;

        // Fallback currency
        public static final String DEFAULT_COUNTRY = "US";
        public static final String DEFAULT_CURRENCY = "USD";

        public static final int DEFAULT_BUDGET_NOTIFICATION_MAX = 99;
        public static final int DEFAULT_BUDGET_NOTIFICATION = 20;
    }

    public static class Transition {
        // Prefixes for dynamic shared transition elements
        public static final String PREFIX_TRIP_TITLE = "prefix_trip_title_";
        public static final String PREFIX_TRIP_PHOTO = "prefix_trip_photo_";
        public static final String PREFIX_TRIP_PHOTO_PROTECTION = "prefix_trip_photo_protection_";
        public static final String PREFIX_TRIP_ATTRIBUTION = "prefix_trip_attribution_";
    }

    // Intent extras
    public static class Extra {
        public static final String EXTRA_TRIP = "extra_trip";
        public static final String EXTRA_TRAVELER = "extra_traveler";
        public static final String EXTRA_EXPENSE = "extra_expense";
        public static final String EXTRA_BUDGET = "extra_budget";
        public static final String EXTRA_PLACE = "extra_place";
        public static final String EXTRA_TRIP_DETAILS_TAB_INDEX = "extra_trip_details_tab_index";
    }

    // Activity requests
    public static class Request {
        public static final int REQUEST_TRIP_FACTORY = 10;
        public static final int REQUEST_EXPENSE_FACTORY = 20;
        public static final int REQUEST_BUDGET_FACTORY = 30;
        public static final int REQUEST_PLACE_FACTORY = 40;
    }

    // Activity results
    public static class Result {
        public static final int RESULT_TRIP_ADDED = 10;
        public static final int RESULT_TRIP_REMOVED = 11;
        public static final int RESULT_TRIP_CHANGED = 12;
        public static final int RESULT_TRIP_ERROR = 13;

        public static final int RESULT_EXPENSE_ADDED = 20;
        public static final int RESULT_EXPENSE_REMOVED = 21;
        public static final int RESULT_EXPENSE_CHANGED = 22;
        public static final int RESULT_EXPENSE_ERROR = 23;

        public static final int RESULT_BUDGET_ADDED = 30;
        public static final int RESULT_BUDGET_REMOVED = 31;
        public static final int RESULT_BUDGET_CHANGED = 32;
        public static final int RESULT_BUDGET_ERROR = 33;

        public static final int RESULT_PLACE_ADDED = 40;
        public static final int RESULT_PLACE_REMOVED = 41;
        public static final int RESULT_PLACE_CHANGED = 42;
        public static final int RESULT_PLACE_ERROR = 43;
    }

    // Intent actions
    public static class Action {
        public static final String ACTION_ADD_PHOTO = "action_add_photo";
        public static final String ACTION_CHANGE_PHOTO = "action_change_photo";
        public static final String ACTION_REMOVE_PHOTO = "action_remove_photo";
        public static final String ACTION_SIGN_OUT_CLEAN_UP = "action_sign_out_clean_up";
        public static final String ACTION_UPDATE_TRIP_LIST = "action_update_trip_list";
    }

    // Shared Preferences
    public static class Preference {
        public static final String PREFERENCE_LAST_USED_COUNTRY = "preference_last_used_country";
        public static final String PREFERENCE_LAST_USED_CURRENCY = "preference_last_used_currency";
        public static final String PREFERENCE_LAST_BUDGET_NOTIFICATION_ID
                = "preference_last_budget_notification_id";
    }

}
