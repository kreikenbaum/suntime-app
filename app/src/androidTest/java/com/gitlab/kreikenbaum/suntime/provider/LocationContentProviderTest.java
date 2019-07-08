package com.gitlab.kreikenbaum.suntime.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import androidx.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LocationContentProviderTest {
    double lat = 23;
    double lon = 53;
    Context context;

    @Before
    public void setup_context() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void delete_create_count() {
        context.getContentResolver().delete(LocationContract.LocationEntry.CONTENT_URI, null, null);
        ContentValues cv = new ContentValues();
        cv.put(LocationContract.LocationEntry.COLUMN_LATITUDE, lat);
        cv.put(LocationContract.LocationEntry.COLUMN_LONGITUDE, lon);
        context.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, cv);
        Cursor cursor = context.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI, null, null, null);
        assertEquals(1, cursor.getCount());
    }

    @Test
    public void insert_then_read() {
        ContentValues cv = new ContentValues();
        cv.put(LocationContract.LocationEntry.COLUMN_LATITUDE, lat);
        cv.put(LocationContract.LocationEntry.COLUMN_LONGITUDE, lon);
        context.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, cv);
        Cursor cursor = context.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI, null, null, null);
        if( cursor != null && cursor.moveToFirst() ) {
//            assertEquals(-1, Arrays.toString(cursor.getBlob(0)));
            ContentValues fromDb = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, fromDb);
            assertEquals(23, (double)fromDb.getAsDouble(LocationContract.LocationEntry.COLUMN_LATITUDE), 0.1);
        } else {
            fail("cursor unavailable");
        }

//        assertEquals(-1, cursor.getcursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_LATITUDE));
    }

    @Test
    public void query() {
    }

    @Test
    public void delete() {
        Context context = InstrumentationRegistry.getTargetContext();
        context.getContentResolver().delete(LocationContract.LocationEntry.CONTENT_URI, null, null);
        Cursor cursor = context.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI, null, null, null);
        assertEquals(0, cursor.getCount());
    }
}