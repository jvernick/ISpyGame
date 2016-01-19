package com.picspy.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.picspy.models.Friend;
import com.picspy.models.Game;
import com.picspy.utils.AppConstants;
import com.picspy.utils.DbContract.FriendEntry;
import com.picspy.utils.DbContract.GameEntry;
import com.picspy.utils.PrefUtil;

import java.util.List;

/**
 * This class handles connections to the local sqlite database and provides
 * methods for performing CRUD operations
 * Created by BrunelAmC on 8/5/2015.
 * TODO make sender_id related to id from friends table?
 */
public class DatabaseHandler extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dbManager.db";
    private static final String SQL_CREATE_FRIENDS_TABLE = "CREATE TABLE " + FriendEntry.TABLE_NAME
            + "("
            + FriendEntry._ID + " INTEGER PRIMARY KEY,"
            + FriendEntry.COLUMN_NAME_USERNAME + " TEXT"
            + ")";
    private static final String SQL_CREATE_GAMES_TABLE = "CREATE TABLE " + GameEntry.TABLE_NAME
            + "("
            + GameEntry._ID + " INTEGER PRIMARY KEY, "
            + GameEntry.COLUMN_NAME_PICTURE + " TEXT, "
            + GameEntry.COLUMN_NAME_SEL + " TEXT, "
            + GameEntry.COLUMN_NAME_HINT + " TEXT, "
            + GameEntry.COLUMN_NAME_GUESS + " INTEGER, "
            + GameEntry.COLUMN_NAME_TIME + " INTEGER, "
            + GameEntry.COLUMN_NAME_VOTE + " BOOLEAN, "
            + GameEntry.COLUMN_NAME_SENDER_ID + " INTEGER, "
            + GameEntry.COLUMN_NAME_CREATED + " TEXT, "
            + GameEntry.COLUMN_NAME_SENDER_NAME + " TEXT"
            + ")";
    private static final String SQL_DELETE_FRIENDS_TABLE =
            "DROP TABLE IF EXISTS " + FriendEntry.TABLE_NAME;
    private static final String SQL_DELETE_GAMES_TABLE =
            "DROP TABLE IF EXISTS " + GameEntry.TABLE_NAME;

    private Context context;
    // Database helper instance
    private static DatabaseHandler _instance;

    // If you change the database schema, you must increment the database version.
    //Default constructor
    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Public method to get DatabaseHandler Object
     * @param context Caller context
     * @return DatabaseHandler Object
     */
    public static DatabaseHandler getInstance(Context context) {
        if (null == _instance) {
            _instance = new DatabaseHandler(context);
        }
        return _instance;
    }

    //Creating tables
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_FRIENDS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_GAMES_TABLE);
        //TODO add statements to fill the tables| Test
    }

    //Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL(SQL_DELETE_GAMES_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_FRIENDS_TABLE);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    public boolean isEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean isEmpty = true;
        Cursor cursor = db.rawQuery("SELECT EXISTS (select 1 from "
                + FriendEntry.TABLE_NAME + ")", null);
        if (cursor != null) {
            cursor.moveToFirst();                       // Always one row returned.
            isEmpty =  cursor.getInt (0) == 0;          // Zero count means empty table.
            Log.d("DbHandler_isEmpty", cursor.getInt(0)+ "");
            cursor.close();
        }

        db.close();
        return isEmpty;
    }
    /**
     * Adds a new friend to the database
     * @param friend Friend to be added
     * @param updated The updated field of the record from the server.
     *                Used to avoid duplicate entries
     */
    public void addFriend(Friend friend, String updated) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FriendEntry.COLUMN_NAME_USERNAME, friend.getUsername()); //Friend name
        values.put(FriendEntry._ID, friend.getId());   //friend_id

        db.insert(FriendEntry.TABLE_NAME, null, values);
        //TODO replace deprecated below
        //PrefUtil.putString(context, AppConstants.LAST_FRIEND_UPDATE_TIME, updated);
        db.close();

    }

    /**
     * Gets a friend from the database by friend id
     * @param friend_id id of the Friend whose record is to be retrieved
     * @return the friend record for specified id
     */
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
        Friend friend = null;
        if (cursor != null) {
            friend = new Friend(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
            cursor.close();
        }
        db.close();
        return friend;
    }

    /**
     * Gets a list of all friends in the database
     * @return A list of all friend records
     */
    //TODO test
    public Cursor getAllFriends() {
        // Select All Query
        String selectQuery = "SELECT * FROM " + FriendEntry.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(selectQuery, null);
    }

    public Cursor getMatchingFriends(String constraint) {
        if (constraint == null || constraint.length() == 0) return getAllFriends();
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {FriendEntry._ID, FriendEntry.COLUMN_NAME_USERNAME};

        String selection = FriendEntry.COLUMN_NAME_USERNAME + " like '%" + constraint + "%'";
        cursor = db.query(
                FriendEntry.TABLE_NAME,
                columns,
                selection,
                null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;

    }

//    public List<Friend> getAllfriends() {
//        List<Friend> friendList = new ArrayList<>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + FriendEntry.TABLE_NAME;
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                Friend friend = new Friend();
//                friend.setId(Integer.parseInt(cursor.getString(0)));
//                friend.setUsername(cursor.getString(1));
//                // Adding friend to list
//                friendList.add(friend);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        // return friend list
//        return friendList;
//    }

    /**
     * Gets the total number of friends in the database
     * @return total number of friends
     */
    public int getFriendCount() {
        String countQuery = "SELECT  * FROM " + FriendEntry.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        db.close();

        // return count
        return cursor.getCount();
    }

    /**
     * Updates the username of a friend
     * @param friend Friend to be modified
     * @return 1 if update was successful otherwise 0
     */
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

    /**
     * Deletes a friend from the database
     * @param friend friend to be deleted
     */
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

    public void addFriends(List<Friend> friends) {
        SQLiteDatabase db = this.getWritableDatabase();
        int maxFriendId = PrefUtil.getInt(context, AppConstants.MAX_FRIEND_RECORD_ID);

        for(Friend friend: friends) {
            if (addFriendHelper(friend, db) == -1) {
                if (maxFriendId < friend.getRecordId()) maxFriendId = friend.getRecordId();
            }
        }
        PrefUtil.putInt(context, AppConstants.MAX_FRIEND_RECORD_ID, maxFriendId);

        db.close();
    }

    private long addFriendHelper(Friend friend, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(FriendEntry.COLUMN_NAME_USERNAME, friend.getUsername()); //Friend name
        values.put(FriendEntry._ID, friend.getId());   //friend_id

        return db.insert(FriendEntry.TABLE_NAME, null, values);
    }

    /**
     * Adds  <b>multiple</b> new games to the database
     * @param games List of games to be added
     * @param max_user_challenge_id The max id of the record in the server. Used to limit
     *                              downloaded content, and to avoid inserting duplicates
     */
    public void addGames(List<Game> games, int max_user_challenge_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int errorCheck = 0;
        for(Game game: games) {
           if (addGameHelper(game, db) == -1) errorCheck = -1;
        }

        if (errorCheck != -1) {
            PrefUtil.putInt(context, AppConstants.MAX_USER_CHALLENGE_ID, max_user_challenge_id);
        }
        db.close();
    }

    /**
     * Helper function to add a single game record
     * @param game game to be added
     * @param db current handler
     */
    private long addGameHelper(Game game, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(GameEntry._ID, game.getId());
        values.put(GameEntry.COLUMN_NAME_PICTURE, game.getPictureName());
        values.put(GameEntry.COLUMN_NAME_SEL, game.getSelection());
        values.put(GameEntry.COLUMN_NAME_HINT, game.getHint());
        values.put(GameEntry.COLUMN_NAME_GUESS, game.getGuess());
        values.put(GameEntry.COLUMN_NAME_TIME, game.getTime());
        values.put(GameEntry.COLUMN_NAME_VOTE, game.isVote());
        values.put(GameEntry.COLUMN_NAME_SENDER_ID, game.getSenderId());
        values.put(GameEntry.COLUMN_NAME_CREATED, game.getCreated());
        values.put(GameEntry.COLUMN_NAME_SENDER_NAME, game.getSenderUsername());

        return db.insert(GameEntry.TABLE_NAME, null, values);
    }

    /**
     * Gets a cursor from which all games in the database can be obtained
     * @return A Cursor for retrieving game records
     */
    public Cursor getAllGames() {
        // Select All Query. Also gets username from Friends table
        String selectQuery = "SELECT * FROM " + GameEntry.TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
       return db.rawQuery(selectQuery, null);
    }
        /**
     * Deletes a game from the database
     * @param game game to be deleted
     */
    public void deleteGame(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = GameEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(game.getId()) };
        db.delete(
                GameEntry.TABLE_NAME,
                selection,
                selectionArgs);
        db.close();
    }
}
