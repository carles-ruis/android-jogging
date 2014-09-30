package com.carles.jogging.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.carles.jogging.C;
import com.carles.jogging.jogging.FootingResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 11/09/14.
 */
public class JoggingSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = JoggingSQLiteHelper.class.getSimpleName();

    private static JoggingSQLiteHelper INSTANCE;
    private static final Gson gson = new Gson();
    private static final Object lock = new Object();

    private static final String TABLE_JOGGING = "jogging";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_START = "start";
    private static final String COLUMN_END = "end";
    private static final String COLUMN_USER = "user";
    private static final String COLUMN_FOOTING_RESULT = "footing_result";
    private static final String COLUMN_REALTIME = "realtime";
    private static final String COLUMN_REALDISTANCE = "realdistance";
    private static final String COLUMN_TOTALTIME = "totaltime";
    private static final String COLUMN_TOTALDISTANCE = "totaldistance";
    private static final String COLUMN_PARENT_ID = "parent_id";

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_EMAIL = "email";

    private static final int LIMIT_JOGGING_LAST_TIMES = 50;

    private static final String SQL_CREATE_JOGGING_TABLE = "CREATE TABLE " + TABLE_JOGGING +
            " (id INTEGER PRIMARY KEY," +
            " start TEXT NOT NULL," +
            " end TEXT NOT NULL," +
            " user TEXT," +
            " footing_result TEXT," +
            " realtime INTEGER NOT NULL," +
            " totaltime INTEGER NOT NULL," +
            " realdistance REAL NOT NULL," +
            " totaldistance REAL NOT NULL," +
            " parent_id INTEGER," +
            " FOREIGN KEY(user) REFERENCES users(name)," +
            " FOREIGN KEY(parent_id)REFERENCES jogging(id) )";

    private static final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
            " (name TEXT PRIMARY KEY," +
            " password TEXT," +
            " email TEXT)";

    private static final String SQL_DROP_JOGGING_TABLE = "DROP TABLE IF EXISTS " + TABLE_JOGGING;
    private static final String SQL_DROP_USERS_TABLE = "DROP TABLE IF EXISTS " + TABLE_USERS;

    private static final String SQL_QUERY_USER = "SELECT name, email FROM " + TABLE_USERS +
            " WHERE name=? AND password=?";

    private static final String SQL_QUERY_JOGGING_LIST_LAST = "SELECT * FROM " + TABLE_JOGGING +
            " WHERE parent_id=0 AND user=? AND totaldistance=? ORDER BY id DESC LIMIT " + LIMIT_JOGGING_LAST_TIMES;

    private static final String SQL_QUERY_JOGGING_LIST_BEST = "SELECT * FROM " + TABLE_JOGGING + " AS t " +
            " WHERE parent_id=0 AND user=? AND totaltime IN " +
                " (SELECT MIN(totaltime) FROM " + TABLE_JOGGING + " AS t2 " +
                " WHERE parent_id=0 AND user=? AND t.totaldistance=t2.totaldistance " +
                " GROUP BY totaldistance) " +
            " ORDER BY totaldistance DESC";

    private static final String SQL_QUERY_BEST_BY_DISTANCE = "SELECT MIN(totaltime) FROM " + TABLE_JOGGING +
            " WHERE parent_id=0 AND user=? AND totaldistance=?";

    private static final String SQL_QUERY_PARTIALS = "SELECT * FROM " + TABLE_JOGGING +
            " WHERE parent_id=? ORDER BY id ASC ";

    public static synchronized JoggingSQLiteHelper getInstance(Context ctx) {
        synchronized (lock) {
            if (INSTANCE == null) {
                // Use the application context, which will ensure that you
                // don't accidentally leak an Activity's context.
                INSTANCE = new JoggingSQLiteHelper(ctx.getApplicationContext());
            }
            return INSTANCE;
        }
    }

    private JoggingSQLiteHelper(Context context) {
        super(context, C.DATABASE_NAME, null, C.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_JOGGING_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DROP_JOGGING_TABLE);
        sqLiteDatabase.execSQL(SQL_DROP_USERS_TABLE);
        onCreate(sqLiteDatabase);
    }

    public long insertUser(UserModel user) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_EMAIL, user.getEmail());
        return db.insert(TABLE_USERS, null, values);
        // there's no need to close a SQLite db in Android. System does it for you
    }

    public long insertJogging(JoggingModel jogging) {
        SQLiteDatabase db = getWritableDatabase();
        long rowId = -1;

        // insert everything in a single transaction for better performance
        try {
            db.beginTransaction();

            // insert "full jogging" object
            rowId = db.insertOrThrow(TABLE_JOGGING, null, getValues(jogging));
            // insert "partial jogging" objects
            if (rowId != -1 && jogging.getPartialsForKilometer()!= null) {
                for (JoggingModel partial : jogging.getPartialsForKilometer()) {
                    db.insertOrThrow(TABLE_JOGGING, null, getValues(partial));
                }
            }
            db.setTransactionSuccessful();// marks a commit

        } catch (SQLException e) {
            Log.e(TAG, "Error inserting jogging to db");

        } finally {
            db.endTransaction(); // will rollback if commit didn't success
        }

        return rowId;
    }

    private ContentValues getValues(JoggingModel jogging) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, jogging.getId());
        values.put(COLUMN_PARENT_ID, jogging.getParentId());
        values.put(COLUMN_END, gson.toJson(new LocationPersisted(jogging.getEnd())));
        values.put(COLUMN_TOTALTIME, jogging.getGoalTime());
        values.put(COLUMN_TOTALDISTANCE, jogging.getGoalDistance());

        // TODO alter table, this fields will be null if the jogging is a partial
        values.put(COLUMN_START, gson.toJson(new LocationPersisted(jogging.getStart())));
        values.put(COLUMN_REALTIME, jogging.getRealTime());
        values.put(COLUMN_REALDISTANCE, jogging.getRealDistance());

        if (jogging.getParentId() == 0l) {
            // values that are only stored if it's a "full" jogging
            values.put(COLUMN_USER, jogging.getUser().getName());
             values.put(COLUMN_FOOTING_RESULT, jogging.getFootingResult().toString());
        }

        return values;
    }

    public void deleteJogging(long id) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            db.delete(TABLE_JOGGING, COLUMN_ID + "=" + id, null);
            db.delete(TABLE_JOGGING, COLUMN_PARENT_ID + "=" + id, null);
            db.setTransactionSuccessful(); // marks a commit

        } catch (SQLException e) {
            Log.e(TAG, "Error deleting jogging from db");
        } finally {
            db.endTransaction();
        }
    }

    public UserModel queryUser(String name, String password) {
        UserModel ret = null;
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(SQL_QUERY_USER, new String[]{name,password});
        if (c!= null && c.moveToFirst()) {
            ret = new UserModel();
            ret.setName(c.getString(c.getColumnIndex(COLUMN_NAME)));
            ret.setEmail(c.getString(c.getColumnIndex(COLUMN_EMAIL)));
        }
        closeCursor(c);
        return ret;
    }

    public List<JoggingModel> queryLastTimes(UserModel user, int distance) {
        List<JoggingModel> ret = new ArrayList<JoggingModel>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(SQL_QUERY_JOGGING_LIST_LAST, new String[]{user.getName(), String.valueOf(distance)});
        if (c != null && c.moveToFirst()) {
            do {
                ret.add(queryJogging(c, user));
            } while (c.moveToNext());
        }
        closeCursor(c);
        return ret;
    }

    private JoggingModel queryJogging(Cursor c, UserModel user) {
        JoggingModel j = new JoggingModel();
        j.setParentId(c.getLong(c.getColumnIndex(COLUMN_PARENT_ID)));
        j.setUser(user);
        j.setId(c.getLong(c.getColumnIndex(COLUMN_ID)));

        String end = c.getString(c.getColumnIndex(COLUMN_END));
        j.setEnd(gson.fromJson(end, LocationPersisted.class).getLocation());
        String start = c.getString(c.getColumnIndex(COLUMN_START));
        j.setStart(gson.fromJson(start, LocationPersisted.class).getLocation());
//        String footingResult = c.getString(c.getColumnIndex(COLUMN_FOOTING_RESULT));
        // only successful footings will be saved
        j.setFootingResult(FootingResult.SUCCESS);

        j.setRealDistance(c.getFloat(c.getColumnIndex(COLUMN_REALDISTANCE)));
        j.setRealTime(c.getLong(c.getColumnIndex(COLUMN_REALTIME)));
        j.setGoalDistance(c.getFloat(c.getColumnIndex(COLUMN_TOTALDISTANCE)));
        j.setGoalTime(c.getLong(c.getColumnIndex(COLUMN_TOTALTIME)));

        return j;
    }

    public List<JoggingModel> queryBestTimes(UserModel user) {
        List<JoggingModel> ret = new ArrayList<JoggingModel>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(SQL_QUERY_JOGGING_LIST_BEST, new String[]{user.getName(), user.getName()});
        if (c!=null && c.moveToFirst()) {
            do {
                ret.add(queryJogging(c, user));
            } while (c.moveToNext());
        }
        closeCursor(c);
        return ret;
    }

    public long queryBestTimeByDistance(UserModel user, float distance) {
        long ret = 0;
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(SQL_QUERY_BEST_BY_DISTANCE, new String[]{user.getName(), String.valueOf(distance)});
        if (c != null && c.moveToFirst()) {
            ret = c.getLong(0);
        }
        closeCursor(c);
        return ret;
    }

    public List<JoggingModel> queryPartials(JoggingModel jogging) {
        List<JoggingModel> ret = new ArrayList<JoggingModel>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(SQL_QUERY_PARTIALS, new String[]{String.valueOf(jogging.getId())});
        if (c!=null && c.moveToFirst()) {
            do {
                ret.add(queryJogging(c, jogging.getUser()));
            } while (c.moveToNext());
        }
        closeCursor(c);
        return ret;
    }

    private void closeCursor(Cursor c) {
        if (c!=null) {
            c.close();
        }
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private static class LocationPersisted {
        private double longitude;
        private double latitude;
        private float accuracy;

        public LocationPersisted() {}

        public LocationPersisted(Location location) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            this.accuracy = location.getAccuracy();
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public float getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(float accuracy) {
            this.accuracy = accuracy;
        }

        public Location getLocation() {
            Location ret = new Location("FUSED");
            ret.setLongitude(longitude);
            ret.setLatitude(latitude);
            ret.setAccuracy(accuracy);
            return ret;
        }

    }
}