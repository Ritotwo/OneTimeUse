package com.app.awqsome.onetimeuse.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.app.awqsome.onetimeuse.Models.Account;
import com.google.firebase.database.DatabaseException;

import java.util.ArrayList;

public class DatabaseHelperClass extends SQLiteOpenHelper {

    private final static String TAG = "DatabaseHelper";
    private final static String DATABASE_NAME = "db";
    private final static String TABLE_NAME = "account";
    private final static String ROW_LABEL = "label";
    private final static String ROW_USER = "user";
    private final static String ROW_ENCRYPTEDDATA = "encrypteddata";
    private final static String ROW_ENCRYPTEDIV = "encrypteddiv";
    private final static String __id = "id";

    private final static int DATABASE_VERSION = 1;

    private SQLiteDatabase db;
    private Context context;

    //region BUILT-IN METHODS

    public DatabaseHelperClass(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + __id + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ROW_LABEL + " TEXT, " + ROW_USER + " TEXT, " + ROW_ENCRYPTEDDATA + " BLOB, " + ROW_ENCRYPTEDIV  + " BLOB);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME);
    }

    //endregion

    public ArrayList<Account> getAllAccounts() {
        ArrayList<Account> accounts = new ArrayList<>();

        try {
            db = this.getWritableDatabase();
            Cursor cs = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);
            if(cs.moveToNext()) {
                do {
                    Account acc = new Account(cs.getInt(cs.getColumnIndex(__id)), cs.getString(cs.getColumnIndex(ROW_LABEL)), cs.getString(cs.getColumnIndex(ROW_USER)), cs.getBlob(cs.getColumnIndex(ROW_ENCRYPTEDDATA)), cs.getBlob(cs.getColumnIndex(ROW_ENCRYPTEDIV)));
                    accounts.add(acc);
                    Log.d(TAG, "Account added : " + acc.toString());
                } while(cs.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }

    public void saveAccount(String label, String user, byte[] data, byte[] iv) {
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ROW_LABEL, label);
            values.put(ROW_USER, user);
            values.put(ROW_ENCRYPTEDDATA, data);
            values.put(ROW_ENCRYPTEDIV, iv);
            db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeAccount(long id) {
        try {
            db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + __id + " = '" + id + "';");
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    public Account getLastItem() {
        try{
            db = this.getWritableDatabase();
            Cursor cs = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);
            cs.moveToLast();
            return new Account(cs.getInt(cs.getColumnIndex(__id)), cs.getString(cs.getColumnIndex(ROW_LABEL)), cs.getString(cs.getColumnIndex(ROW_USER)), cs.getBlob(cs.getColumnIndex(ROW_ENCRYPTEDDATA)), cs.getBlob(cs.getColumnIndex(ROW_ENCRYPTEDIV)));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isDatabaseEmpty() {
        try{
            db = this.getReadableDatabase();
            Cursor cs = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);
            if(cs.moveToFirst()) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private void openDatabase() {
        //try to open database 3 times
        boolean isOpened = false;

        if(!isOpened) {
            for(int i = 0; i < 3; i++) {
                db = this.getWritableDatabase();
                if(db != null) {
                    isOpened = true;
                    return;
                }
            }
        }
    }

    public void close() {
        db.close();
    }
}
