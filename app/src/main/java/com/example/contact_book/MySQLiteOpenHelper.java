package com.example.contact_book;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String CONTACT_LIST_DATABASE_NAME = "contact_list_database";
    private static final String RECORD_LIST_DATABASE_NAME = "record_list_database";
    private static final String NUMBER_PLACE_DATABASE_NAME = "number_place_database";

    //上下文
    private final Context context;

    //创建联系人数据库用的SQL语句
    String create_contact_list_database_sql=
            "create table " +CONTACT_LIST_DATABASE_NAME+ " ("
                    +"name text NOT NULL" +",nickname text"
                    +",phone text NOT NULL UNIQUE"+",phoneType integer"
                    +",company text"
                    +",email text"+",remark text"
                    +",address text"
                    +",note text"
                    +",star integer"
                    +",relationship integer"
                    +",avatar blob"
                    +", primary key(phone)"
                    +")";

    //创建通话记录数据库
    String create_record_list_database_sql =
            "create table " + RECORD_LIST_DATABASE_NAME + " ("
                    + "id integer primary key autoincrement"
                    + ",name text"
                    + ",number text NOT NULL"
                    + ",date time"
                    + ",time text"
                    + ",type text"
                    + ")";
    //创建归属地数据库
    String create_number_place_database_sql =
            "create table " + NUMBER_PLACE_DATABASE_NAME + " ("
                    + "number text primary key"
                    + ",place text"
                    + ")";
    MySQLiteOpenHelper(Context context_in) {
        super(context_in, "CONTACT_BOOK", null, DATABASE_VERSION);
        context = context_in;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建联系人数据库、通话记录数据库
        db.execSQL(create_contact_list_database_sql);
        db.execSQL(create_record_list_database_sql);
        db.execSQL(create_number_place_database_sql);

        //调试时使用
        Toast.makeText(context, "Created！", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + CONTACT_LIST_DATABASE_NAME);
        db.execSQL("drop table if exists " + RECORD_LIST_DATABASE_NAME);
        db.execSQL("drop table if exists " + NUMBER_PLACE_DATABASE_NAME);
        onCreate(db);
    }
}
