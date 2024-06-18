package com.mautilus.emcare;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.mautilus.emcare.entity.Action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.RequiresApi;

public class ActionTracker {

    private static final int KEYBOARD_EVENT_TYPE = 16;

    static private SQLiteDatabase db;

    static private ParametersHelper parameters;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static public void LogKeyboardEvent(Context context) {
        db = DatabaseConnection.getDatabase(context);

        PushData push = new PushData();
        ContentValues values = new ContentValues();

        values.put(Action.FIELD_ADDED_NAME, 1);
        values.put(Action.FIELD_REMOVED_NAME, 0);

        values.put(Action.FIELD_TYPE_NAME, KEYBOARD_EVENT_TYPE);

        // Ponemos el tiempo según corresponda.
        Calendar nowInstance = Calendar.getInstance();
        Date currentTimeMin = nowInstance.getTime();
        values.put(Action.FIELD_TIME_NAME, currentTimeMin.getTime());

//        Log.i(TAG, "onAccessibilityEvent: " + accessibilityEvent.getEventType());

        Long id = db.insert(Action.TABLE_NAME, Action.FIELD_ID_NAME, values);

        push.update(context);
    }


}