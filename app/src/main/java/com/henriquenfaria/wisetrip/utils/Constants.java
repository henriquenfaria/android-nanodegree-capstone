package com.henriquenfaria.wisetrip.utils;


// Class that holds all app's public constants
public class Constants {
    // Global constants
    public static class Global {
        // Max Trip destinations
        public static final int MAX_DESTINATIONS = 10;
        // Max Trip travelers
        public static final int MAX_TRAVELERS = 10;
        // Max height for Trip photo
        public static final String MAX_PHOTO_HEIGHT = "400";
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
        public static final String ACTION_GET_PHOTO = "action_get_photo";
        public static final String ACTION_DELETE_PHOTO = "action_delete_photo";
    }
}
