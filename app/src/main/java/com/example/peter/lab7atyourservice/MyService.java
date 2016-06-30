package com.example.peter.lab7atyourservice;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyService extends Service implements IAsyncResponse {
    FeedReaderDbHelper mDbHelper;

    public MyService() {
    }

    private boolean started = false;
    private boolean firstTime = true;
    private long wait;
    private long id;

    @Override
    public void onCreate() {
        super.onCreate();

        id = 0;

        mDbHelper = new FeedReaderDbHelper(this);
        Log.d("MyService", "Background service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //in this case we only start the background running loop once
        if (!started && intent != null) {
            wait = intent.getLongExtra("waitTime", 10000);
            Log.d("MyService", "Background service onStartCommand with wait: " + wait + "ms");
            started = true;

            doBackgroundThing(wait);
        }
        return START_STICKY;
    }

    private void doBackgroundThing(final long wait) {


        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {

                if (firstTime) {
                    firstTime = false;
                    currentWeather();
                } else {
                    try {
                        Thread.sleep(wait);
                        currentWeather();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                //if Service is still running, keep doing this recursively
                if (started) {
                    doBackgroundThing(wait);
                }
            }
        };
        task.execute();
    }


    private void broadcastTaskResult(ArrayList<WeatherObject> result) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("weatherService");
        broadcastIntent.putExtra("WeatherObject", result);
        Log.d("MyService", "Broadcasting!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    @Override
    public void onDestroy() {
        started = false;
        Log.d("MyService", "Background service destroyed");

        mDbHelper.destroyDB();
        super.onDestroy();
    }

    @Override
    public void processFinish(WeatherObject wo) {
        Log.d("TESTING", "processFinish. Temp is: " + wo.getTemp());
        ArrayList<WeatherObject> list = getUpdatedDB(wo);
        broadcastTaskResult(list);
    }

    private ArrayList<WeatherObject> getUpdatedDB(WeatherObject wo) {
        ArrayList<WeatherObject> list = new ArrayList<WeatherObject>();
        putDb(String.valueOf(id), wo.getTime(), wo.getTemp(), wo.getWindSpeed(), wo.getWeatherDes(), wo.getCity());
        id++;

        for (long tempID = id - 1; tempID > id - 48 - 1 && tempID >= 0; tempID--) {
            WeatherObject newWo = readDb(tempID);
            list.add(newWo);
        }
        return list;
    }

    public void currentWeather() {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=Aarhus&APPID=382d5441eadc62d861fd0a5abd8e8002&units=metric";
        //The async task can be executed only once, so a new instance is created
        WeatherHandler weatherHandler = new WeatherHandler();
        weatherHandler.delegate = this;
        weatherHandler.execute(url);
    }

    private void putDb(String id, String time, String temp, String wind, String des, String city) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID, id);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TIME, time);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TEMP, temp);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_WIND, wind);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DES, des);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CITY, city);

        // Insert the new row
        db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
    }

    private WeatherObject readDb(long rowId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TIME,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TEMP,
                FeedReaderContract.FeedEntry.COLUMN_NAME_WIND,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DES,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CITY
        };
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(rowId)};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_DES + " DESC";

        Cursor c = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        c.moveToFirst();


        String time = c.getString(c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TIME));
        String temp = c.getString(c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TEMP));
        String wind = c.getString(c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_WIND));
        String des = c.getString(c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_DES));
        String city = c.getString(c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_CITY));

        WeatherObject wo = new WeatherObject(des, wind, temp, city, time);
        return wo;
    }


    public class MyServiceBinder extends Binder {

        MyService getService() {
            return MyService.this;
        }

    }

    private final IBinder binder = new MyServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
