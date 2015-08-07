package com.picspy.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.picspy.models.Friend;
import com.picspy.utils.FriendContract.FriendEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BrunelAmC on 8/5/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "friendsManager.db";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + FriendEntry.TABLE_NAME + "("
            + FriendEntry._ID + " INTEGER PRIMARY KEY," + FriendEntry.COLUMN_NAME_USERNAME
            + " TEXT," + ")";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FriendEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    //Creating tables
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_FRIENDS_TABLE = SQL_CREATE_ENTRIES;
       sqLiteDatabase.execSQL(CREATE_FRIENDS_TABLE);
    }

    //Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
       sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        // Create tables again
        onCreate(sqLiteDatabase);
    }

    // Adding new friend
    public void addFriend(Friend friend) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FriendEntry.COLUMN_NAME_USERNAME, friend.getUsername()); //Friend name
        values.put(FriendEntry._ID, friend.getId());   //friend_id

        db.insert(FriendEntry.TABLE_NAME, FriendEntry.COlUMN_NAME_NULLABLE, values);
        db.close();

    }

    //Getting a single friend
    public Friend getFriend(int friend_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] projection = {FriendEntry._ID, FriendEntry.COLUMN_NAME_USERNAME};
        String selection = FriendEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(friend_id)};
        Cursor cursor = db.query(
                FriendEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Friend friend = new Friend(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
        db.close();
        return friend;
    }

    // Getting All Friends
    public List<Friend> getAllfriends() {
        List<Friend> friendList = new ArrayList<Friend>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + FriendEntry.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Friend friend = new Friend();
                friend.setId(Integer.parseInt(cursor.getString(0)));
                friend.setUsername(cursor.getString(1));
                // Adding friend to list
                friendList.add(friend);
            } while (cursor.moveToNext());
        }

        // return friend list
        return friendList;
    }

    // Getting friend Count
    public int getFriendCount() {
        String countQuery = "SELECT  * FROM " + FriendEntry.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    // Updating single friend
    public int updateFriend(Friend friend) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FriendEntry.COLUMN_NAME_USERNAME, friend.getUsername());
        String whereClause = FriendEntry._ID + " = ?";
        String[] whereArgs  = {String.valueOf(friend.getId())};

        // updating row
        return db.update(FriendEntry.TABLE_NAME,
                values,
                whereClause,
                whereArgs);
    }

    // Deleting single friend
    public void deleteFriend(Friend friend) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = FriendEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(friend.getId()) };
        db.delete(
                FriendEntry.TABLE_NAME,
                selection,
               selectionArgs);
        db.close();
    }
}
