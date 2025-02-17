package com.mautilus.emcare;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mautilus.emcare.entity.Action;

import java.util.Calendar;
import java.util.Date;

public class ActionTracker {

    private static final int KEYBOARD_EVENT_TYPE = 16;

    static public void LogKeyboardEvent(Context context) {
        SQLiteDatabase db = DatabaseConnection.getDatabase(context);

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

        db.insert(Action.TABLE_NAME, Action.FIELD_ID_NAME, values);

        push.update(context);
    }


}