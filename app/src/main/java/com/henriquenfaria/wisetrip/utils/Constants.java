package com.henriquenfaria.wisetrip.utils;


// Class that holds all app's public constants
public class Constants {
    // Global constants
    public static class Global {
        // Max Trip destinations
        public static final int MAX_DESTINATIONS = 10;
        // Max Trip travelers
        public static final int MAX_TRAVELERS = 10;
        // Internal Storage directory for destination photos
        public static final String DESTINATION_PHOTO_DIR = "destinations";
    }

    // Intent extras
    public static class Extra {
        public static final String EXTRA_TRIP = "extra_trip";
        public static final String EXTRA_TRAVELER = "extra_traveler";
    }


    // Intent actions
    public static class Action {
        public static final String ACTION_ADD_PHOTO = "action_add_photo";
        public static final String ACTION_CHANGE_PHOTO = "action_change_photo";
        public static final String ACTION_REMOVE_PHOTO = "action_remove_photo";
        public static final String ACTION_SIGN_OUT_CLEAN_UP = "action_sign_out_clean_up";
        public static final String ACTION_UPDATE_TRIP_LIST = "action_update_trip_list";
    }
}
