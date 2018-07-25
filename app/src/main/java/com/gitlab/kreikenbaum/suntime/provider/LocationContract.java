package com.gitlab.kreikenbaum.suntime.provider;
// taken from mygarden branch TWID.03-Solution

/*
* Copyright (C) 2017 The Android Open Source Project
* Copyright (C) 2018 github.com/serv-inc
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.net.Uri;
import android.provider.BaseColumns;

public class LocationContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.gitlab.kreikenbaum.suntime";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "locations" directory
    public static final String PATH_LOCATIONS = "locations";
    public static final String PATH_LAST_LOCATION = "last_location";

    public static final long INVALID_LOCATION_ID = -1;

    public static final class LocationEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build();
        public static final Uri CONTENT_URI_LAST =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LAST_LOCATION).build();


        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
    }
}
