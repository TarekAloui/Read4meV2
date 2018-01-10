package com.aloui.tarek.read4me.Other;

import android.content.SharedPreferences;
import android.provider.ContactsContract;

import com.aloui.tarek.read4me.Models.Book;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by Tarek on 06/01/2018.
 */

public class Database {

    //CONSTANTS
    private static final String CurrentBookPath = "curBook";
    private static final String SharedPref = "sharedPref";
    private static final String PASSED_BOOK_KEY = "Book_Key";

    public static int readInd = 0;
    public static ArrayList<String> pages;

    SharedPreferences mSettings;
    SharedPreferences.Editor mEditor;

    GsonBuilder gsonb = new GsonBuilder();
    Gson mGson = gsonb.create();

    public Database(SharedPreferences settings)
    {
        mSettings = settings;
        mEditor = mSettings.edit();
    }

    public boolean saveBook(Book book, String path)
    {
        try {
            String writeValue = mGson.toJson(book);
            mEditor.putString(path, writeValue);
            mEditor.commit();
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public boolean saveCurBookHash(String curBookKey)
    {
        try {
            mEditor.putString(getCurrentBookPath(), curBookKey);
            mEditor.commit();
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public boolean saveBook(Book book) //SAVE TO CURRENT BOOK
    {
        try {
            String writeValue = mGson.toJson(book);
            mEditor.putString(getCurrentBookPath(), writeValue);
            mEditor.commit();
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public Book getBook(String path)
    {
        String loadValue = mSettings.getString(path, "");
        if(loadValue.equals("")) return  null;
        Book book = mGson.fromJson(loadValue, Book.class);
        return book;
    }

    public String getCurrentBookKey()
    {
        String bookKey = mSettings.getString(getCurrentBookPath(), "");
        return bookKey;
    }

    //Getters
    public static String getCurrentBookPath() {
        return CurrentBookPath;
    }

    public static String getSharedPref() {
        return SharedPref;
    }

    public static String getPassedBookKey() {
        return PASSED_BOOK_KEY;
    }
}
