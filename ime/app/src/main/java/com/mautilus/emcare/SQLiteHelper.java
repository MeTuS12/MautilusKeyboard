package com.mautilus.emcare;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mautilus.emcare.entity.Action;
import com.mautilus.emcare.entity.Parameters;

import androidx.annotation.Nullable;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Action.CREATE_TABLE);
        db.execSQL(Parameters.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Action.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Parameters.TABLE_NAME);
        db.execSQL(Action.CREATE_TABLE);
        db.execSQL(Parameters.CREATE_TABLE);
    }
}