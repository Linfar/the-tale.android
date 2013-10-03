package org.thetale;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Andrey.Titov on 9/19/13.
 */
public class DataProvider extends ContentProvider {
    SQLiteOpenHelper mySqLiteOpenHelper;

    // Used for the UriMacher
    private static final int ALL_JOURNAL_ITEMS = 10;
    private static final int JOURNAL_ITEM_BY_ID = 20;
    private static final int ALL_HERO_DATA = 30;

    public static final Set<String> ourAllAvailableColumns = new HashSet<String>();

    public static final String AUTHORITY = "org.thetale";

    public static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, ServerContract.HeroData.CONTENT_PATH, ALL_HERO_DATA);
        sURIMatcher.addURI(AUTHORITY, ServerContract.Journal.CONTENT_PATH, ALL_JOURNAL_ITEMS);
        sURIMatcher.addURI(AUTHORITY, ServerContract.Journal.CONTENT_PATH + "/#", JOURNAL_ITEM_BY_ID);
    }

    private static class MyBD extends SQLiteOpenHelper {

        public static final int VERSION = 10;
        public static final String NAME = "the-tale.db";

        public MyBD(Context context) {
            super(context, NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            ServerContract.Journal.onCreate(sqLiteDatabase);
            ServerContract.HeroData.onCreate(sqLiteDatabase);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            ServerContract.Journal.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
            ServerContract.HeroData.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        }
    }

    @Override
    public boolean onCreate() {
        mySqLiteOpenHelper = new MyBD(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case JOURNAL_ITEM_BY_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(ServerContract.Journal.CONTENT_PATH + "=" + uri.getLastPathSegment());
            case ALL_JOURNAL_ITEMS:
                queryBuilder.setTables(ServerContract.Journal.CONTENT_PATH);
                break;
            case ALL_HERO_DATA:
                queryBuilder.setTables(ServerContract.HeroData.CONTENT_PATH);
//                queryBuilder.appendWhere(ServerContract.HeroData.CONTENT_PATH + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mySqLiteOpenHelper.getWritableDatabase();
        if (db == null) return null;
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        if (cursor == null) return null;
        // Make sure that potential listeners are getting notified
        Context context = getContext();
        if (context == null) return null;
        cursor.setNotificationUri(context.getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mySqLiteOpenHelper.getWritableDatabase();
        if (sqlDB == null) return null;
        int rowsDeleted = 0;
        long id = 0;
        Uri res = null;
        switch (uriType) {
            case ALL_JOURNAL_ITEMS:
                id = sqlDB.insertOrThrow(ServerContract.Journal.CONTENT_PATH, null, contentValues);
                res = ServerContract.Journal.CONTENT_URI;
                break;
            case ALL_HERO_DATA:
                id = sqlDB.insertOrThrow(ServerContract.HeroData.CONTENT_PATH, null, contentValues);
                res = ServerContract.HeroData.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        if (context != null) context.getContentResolver().notifyChange(uri, null);
        return res;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mySqLiteOpenHelper.getWritableDatabase();
        if (sqlDB == null) return 0;
        int rowsDeleted = 0;
        switch (uriType) {
            case ALL_JOURNAL_ITEMS:
                rowsDeleted = sqlDB.delete(ServerContract.Journal.CONTENT_PATH, selection,
                        selectionArgs);
                break;
            case JOURNAL_ITEM_BY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ServerContract.Journal.CONTENT_PATH,
                            ServerContract.Journal._ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ServerContract.Journal.CONTENT_PATH,
                            ServerContract.Journal._ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            case ALL_HERO_DATA:
                rowsDeleted = sqlDB.delete(ServerContract.HeroData.CONTENT_PATH, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        if (context == null) return rowsDeleted;
        context.getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mySqLiteOpenHelper.getWritableDatabase();
        if (sqlDB == null) return 0;
        int rowsUpdated = 0;
        switch (uriType) {
            case ALL_JOURNAL_ITEMS:
                rowsUpdated = sqlDB.update(ServerContract.Journal.CONTENT_PATH,
                        values,
                        selection,
                        selectionArgs);
                break;
            case JOURNAL_ITEM_BY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ServerContract.Journal.CONTENT_PATH,
                            values,
                            ServerContract.Journal._ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(ServerContract.Journal.CONTENT_PATH,
                            values,
                            ServerContract.Journal._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        if (context == null) return rowsUpdated;
        context.getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            // Check if all columns which are requested are available
            if (!ourAllAvailableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection: " + requestedColumns.toString());
            }
        }
    }
}
