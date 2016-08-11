package com.kaist.safetydriving.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "db";
    static final String TYPE = "msg_type";
    static final String SENDER = "msg_sender";
    static final String CONTENT = "msg_con";
    static final String ARRIVALTIME = "ariv_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE history (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+TYPE+" INTEGER, "+SENDER+" TEXT, "+CONTENT+" TEXT, "+ARRIVALTIME+" datetime);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        android.util.Log.w("history", "Upgrading database, which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    public String getConditionByDevice(String device ) {
        return "AA";
    }

    public void newHistoryData(int type, String sender, String content, String arrival_time) {
        ContentValues cv = new ContentValues();
        cv.put(TYPE, type);
        cv.put(SENDER, sender);
        cv.put(CONTENT, content);
        cv.put(ARRIVALTIME, arrival_time);
        this.getWritableDatabase().insert("history", content, cv);
    }

    public static final class Fields implements BaseColumns
    {
        public static final int TYPE_GENERAL		= 0;
    }
}
