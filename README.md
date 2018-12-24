Wise Trip
======

This is the Capstone Project of Udacity's Android Developer Nanodegree.

Wise Trip helps you organize your business and personal trips by recording your expenses,
budget and the places you want to visit.

1. Record travel expenses: Add new expenses with a press of a button, using the app or the
home screen widget. Select the currency, the date, the category and add photos to an
place.
2. Stop overspending: Create different budgets for each currency or category. It automatically
converts it to your home country currency.
3. Manage your itinerary: Never get lost again! Add places you want to visit for each trip and
easily get directions to it.
4. Never lost your data again: All your trip data is securely saved in the cloud.


Screens
======

![alt text](https://github.com/henriquenfaria/android-nanodegree-capstone/blob/master/art/initial_screen.png "Initial screen")
![alt text](https://github.com/henriquenfaria/android-nanodegree-capstone/blob/master/art/trip_list_screen.png "Trip list screen")
![alt text](https://github.com/henriquenfaria/android-nanodegree-capstone/blob/master/art/create_trip_screen.png "Create trip screen")
![alt text](https://github.com/henriquenfaria/android-nanodegree-capstone/blob/master/art/expenses_budgets_screen.png "Expenses and Budgets screen")
![alt text](https://github.com/henriquenfaria/android-nanodegree-capstone/blob/master/art/place_selection_screen.png "Place selection screen")
![alt text](https://github.com/henriquenfaria/android-nanodegree-capstone/blob/master/art/place_details_screen.png "Place details screen")


Instructions
======

1. Create a Firebase project and add a new app with the following package name: `com.henriquenfaria.wisetrip`.
2. Enable Firebase Authentication with e-mail and Google account sign-in.
3. Create a Realtime Database and set the rules in the section below.
4. Download your `google-services.json` file and put it inside the app folder.
5. Go to your Google API console and enable Google Maps and Places API for Android.
6. Put the API Key on your user's `gradle.properties` file: `google_geo_api_manifest_key=<API_KEY>`.
7. You are good to go now.

Firebase Realtime Database rules
======

```
{
  "rules": {
    "trips": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "attributions": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "expenses": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "budgets": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "places": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

License
------

> Copyright 2017 Henrique Nunes Faria

> Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

> Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
