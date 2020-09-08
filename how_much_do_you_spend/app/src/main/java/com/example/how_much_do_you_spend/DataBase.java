package com.example.how_much_do_you_spend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class DataBase {
    public static final class CreateDB implements BaseColumns {
        public static final String TIME = "time";
        public static final String MOBILE = "mobile";
        public static final String WIFI = "wifi";
        public static final String COUNT = "count";
        public static final String DATE = "date";
        public static final String _TABLENAME0 = "temp_data";

        public static final String PACKAGE = "package";
        public static final String CATEGORYNAME = "name";
        public static final String _TABLENAME1 = "category";


        public static final String _CREATE1 = "create table if not exists " + _TABLENAME1 + "("
                + _ID + " integer primary key autoincrement, "
                + PACKAGE + " text not null , "
                + CATEGORYNAME + " text not null);";


        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("
                + _ID + " integer primary key autoincrement, "
                + COUNT + " integer not null , "
                + MOBILE + " integer not null , "
                + WIFI + " integer not null , "
                + TIME + " integer not null , "
                + DATE + " text not null);";
    }

    public static class DbOpenHelper {
        private static final String DATABASE_NAME = "InnerDatabase(SQLite).db";
        private static final int DATABASE_VERSION = 1;
        public static SQLiteDatabase mDB;
        private DatabaseHelper mDBHelper;
        private Context mCtx;

        private class DatabaseHelper extends SQLiteOpenHelper {
            public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
                super(context, name, factory, version);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(DataBase.CreateDB._CREATE0);
                db.execSQL(DataBase.CreateDB._CREATE1);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateDB._TABLENAME0);
                db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateDB._TABLENAME1);
                onCreate(db);
            }
        }

        public DbOpenHelper(Context context) {
            this.mCtx = context;
        }

        public DbOpenHelper open() throws SQLException {
            mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
            mDB = mDBHelper.getWritableDatabase();
            return this;
        }
        public long insertColumn(int count, int time, int mobile, int wifi, String date) {
            ContentValues values = new ContentValues();

            values.put(CreateDB.COUNT, count);
            values.put(CreateDB.MOBILE, mobile/1024);
            values.put(CreateDB.WIFI, wifi/1024);
            values.put(CreateDB.TIME, time);
            values.put(CreateDB.DATE, date);

            return mDB.insert(DataBase.CreateDB._TABLENAME0, null, values);
        }

        public long insertColumn(String packageName,String category){
            ContentValues values = new ContentValues();

            values.put(CreateDB.PACKAGE,packageName);
            values.put(CreateDB.CATEGORYNAME,category);

            return mDB.insert(DataBase.CreateDB._TABLENAME1,null, values);
        }

        public String getCategory(String p) {
            Cursor c = mDB.query(DataBase.CreateDB._TABLENAME1, new String[]{"name"}, "package = ?", new String[]{p}, null, null, null);
            if(c.getCount()==0)
                return null;
            c.moveToLast();

            return c.getString(c.getColumnIndex("name"));
        }

        public int getLength() {
            Cursor c = mDB.query(DataBase.CreateDB._TABLENAME0, null, null, null, null, null, null);
            if(c==null)
                return 0;
            return c.getCount();
        }

        public Cursor getData(){
            return mDB.query(DataBase.CreateDB._TABLENAME0, null, null, null, null, null, null);
        }

        public int updateData(int count, int time, long mobile, long wifi, String date) {
            ContentValues values = new ContentValues();

            values.put(CreateDB.COUNT, count);
            values.put(CreateDB.MOBILE, (int)(mobile/1024));
            values.put(CreateDB.WIFI, (int)(wifi/1024));
            values.put(CreateDB.TIME, time);
            values.put(CreateDB.DATE, date);

            return mDB.update(DataBase.CreateDB._TABLENAME0, values, "_id = 1", null);
        }

        public void initData(){
            ContentValues values = new ContentValues();

            values.put(CreateDB.COUNT, 0);
            values.put(CreateDB.MOBILE, 0);
            values.put(CreateDB.WIFI, 0);
            values.put(CreateDB.TIME, 0);
            values.put(CreateDB.DATE, DayManager.getInstance().dateToString(DayManager.getInstance().getToday()));

            mDB.update(DataBase.CreateDB._TABLENAME0, values, "_id = 1", null);
        }

        public void create() {
            mDBHelper.onCreate(mDB);
        }

        public void close() {
            mDB.close();
        }
    }
}

