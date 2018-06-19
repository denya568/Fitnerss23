package ru.wt23.planner23;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final int VERSION = 8;
    public static final String NAME_OF_DB = "tasks";
    public static final String TABLE_COMPLETED = "completed";
    public static final String TABLE_NON_COMPLETED = "noncompleted";

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String COLOR = "color";

    public DBHelper(Context context) {
        super(context, NAME_OF_DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_COMPLETED + " (" + ID + " integer primary key, " + NAME + " text not null, " + COLOR + " integer);");
        db.execSQL("create table " + TABLE_NON_COMPLETED + " (" + ID + " integer primary key, " + NAME + " text not null, " + COLOR + " integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("drop table if exists " + TABLE_COMPLETED + ";");
        db.execSQL("drop table if exists " + TABLE_NON_COMPLETED + ";");

        onCreate(db);
    }
}
