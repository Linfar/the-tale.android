package org.thetale;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrey.Titov on 9/19/13.
 */
public class ServerContract {
    public static final String AUTHORITY = DataProvider.AUTHORITY;

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static class Table implements BaseColumns {
        protected ArrayList<String> columns = new ArrayList<String>(Arrays.asList(_ID, _COUNT));

        public Table() {}

        public Table(String tableName) {}

        public String[] getAllColumns() {
            return columns.toArray(new String[columns.size()]);
        }
    }

    public interface JournalColumns {
        public static final String TIME = "timep";
        public static final String TIME_V = "timev";
        public static final String DATE_V = "datev";
        public static final String DESCRIPTION = "description";
    }

    public interface HeroDataColumns {
        public static final String EXPERIENCE = "experience";
        public static final String RACE = "race";
        public static final String HEALTH = "health";
        public static final String MAX_HEALTH = "max_health";
        public static final String NAME = "name";
        public static final String LEVEL = "level";
        public static final String GENDER = "gender";
        public static final String EXP_TO_NEXT_LEVEL = "experience_to_level";
        public static final String DESTINY_POINTS = "destiny_points";
        public static final String CURRENT_ACTION_DESCRIPTION = "current_action_description";
    }

    // TODO
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Column {}

    public static final class Journal extends Table implements JournalColumns {
        public static final String[] COLUMNS = new String[]{TIME, TIME_V, DATE_V, DESCRIPTION, _COUNT, _ID};

        {
            columns.addAll(Arrays.asList(COLUMNS));
        }

        static {
            DataProvider.ourAllAvailableColumns.addAll(Arrays.asList(TIME, TIME_V, DATE_V, DESCRIPTION, _COUNT, _ID));
        }
        public static final String CONTENT_PATH = "journalData";
        public static final Uri CONTENT_URI = Uri.parse("content://" + DataProvider.AUTHORITY + "/" + CONTENT_PATH);// Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY + "." + CONTENT_PATH;

        private static final String DATABASE_CREATE = "create table "
                + CONTENT_PATH
                + "("
                + _ID + " integer primary key autoincrement, "
                + _COUNT + " integer not null, "
                + TIME + " integer not null, "
                + DESCRIPTION + " text not null, "
                + DATE_V + " text not null, "
                + TIME_V + " text not null"
                + ");";

        public static void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE);
        }

        public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                     int newVersion) {
            Log.w(Journal.class.getName(), "Upgrading database from version "
                    + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            database.execSQL("DROP TABLE IF EXISTS " + CONTENT_PATH);
            onCreate(database);
        }
    }

    public static final class HeroData implements BaseColumns, HeroDataColumns {
        public static final String CONTENT_PATH = "heroData";
        public static final Uri CONTENT_URI = Uri.parse("content://" + DataProvider.AUTHORITY + "/" + CONTENT_PATH);// Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY + "." + CONTENT_PATH;
        public static final String[] ALL_COLUMNS = {EXP_TO_NEXT_LEVEL, EXPERIENCE, RACE, HEALTH, MAX_HEALTH, NAME, LEVEL, GENDER, DESTINY_POINTS, CURRENT_ACTION_DESCRIPTION};
        static {
            DataProvider.ourAllAvailableColumns.addAll(Arrays.asList(ALL_COLUMNS));
        }

        private static final String DATABASE_CREATE = "create table "
                + CONTENT_PATH
                + "("
                + _ID + " integer primary key autoincrement, "
                + _COUNT + " integer not null, "
                + EXP_TO_NEXT_LEVEL + " integer not null, "
                + EXPERIENCE + " integer not null,"
                + RACE + " text not null,"
                + HEALTH + " integer not null,"
                + MAX_HEALTH + " integer not null,"
                + NAME + " text not null,"
                + LEVEL + " integer not null,"
                + GENDER + " text not null,"
                + DESTINY_POINTS + " integer not null,"
                + CURRENT_ACTION_DESCRIPTION + " text not null"
                + ");";

        public static void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE);
        }

        public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                     int newVersion) {
            Log.w(Journal.class.getName(), "Upgrading database from version "
                    + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            database.execSQL("DROP TABLE IF EXISTS " + CONTENT_PATH);
            onCreate(database);
        }
    }
}
