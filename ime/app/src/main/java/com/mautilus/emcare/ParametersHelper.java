package com.mautilus.emcare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mautilus.emcare.entity.Parameters;

public class ParametersHelper {


    private String[] columns = {Parameters.FIELD_ID_NAME, Parameters.FIELD_KEY_NAME, Parameters.FIELD_VALUE_NAME};

    private SQLiteDatabase db;

    public ParametersHelper(Context appContext) {
        db = DatabaseConnection.getDatabase(appContext);
    }

    private Cursor getByKey(String key) {
        return db.query(Parameters.TABLE_NAME, columns,
                Parameters.FIELD_KEY_NAME + "=?", new String[]{key}, null, null, null);
    }


    public String get(String key) {
        Cursor cursor = getByKey(key);
        int count = cursor.getCount();

        if (count == 0) {
            return null;
        }

        cursor.moveToFirst();
        return cursor.getString(2);
    }

    public void  set(String key, String value) {
        Cursor cursor = getByKey(key);
        int count = cursor.getCount();

        ContentValues values = new ContentValues();

        values.put(Parameters.FIELD_KEY_NAME, key);
        values.put(Parameters.FIELD_VALUE_NAME, value);

        if (count == 0) {
            db.insert(Parameters.TABLE_NAME, Parameters.FIELD_ID_NAME, values);
        } else {
            db.update(Parameters.TABLE_NAME, values, Parameters.FIELD_KEY_NAME + "=?", new String[]{key});
        }
    }


}