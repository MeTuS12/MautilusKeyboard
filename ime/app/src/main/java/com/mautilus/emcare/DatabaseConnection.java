package com.mautilus.emcare;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseConnection {

    public final static String DATABASE_NAME = "db_em_local";

    private final static int version = 1;

    static SQLiteDatabase getDatabase(Context context) {
        SQLiteHelper conn = new SQLiteHelper(context, "db_em_local", null, version);
        return conn.getWritableDatabase();
    }

}