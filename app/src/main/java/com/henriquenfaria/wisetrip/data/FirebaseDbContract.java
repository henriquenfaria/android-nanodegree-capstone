package com.henriquenfaria.wisetrip.data;

/**
 * Helper class to organize the Firebase db schema
 */
public class FirebaseDbContract {

    public static final class Trips {
        public final static String PATH_TRIPS = "trips";
        public final static String PATH_TITLE = "title";
    }

    public static final class Attributions {
        public final static String PATH_ATTRIBUTIONS = "attributions";
    }

    public static final class Expenses {
        public final static String PATH_EXPENSES = "expenses";
    }

    public static final class Budgets {
        public final static String PATH_BUDGETS = "budgets";
    }

    public static final class Places {
        public final static String PATH_PLACES = "places";
    }
}
