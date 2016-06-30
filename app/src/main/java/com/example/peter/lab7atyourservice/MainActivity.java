package com.example.peter.lab7atyourservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.peter.lab7atyourservice.FeedReaderContract;
import com.example.peter.lab7atyourservice.FeedReaderDbHelper;
import com.example.peter.lab7atyourservice.MyService;
import com.example.peter.lab7atyourservice.R;
import com.example.peter.lab7atyourservice.WeatherHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private CustomAdapter adapter;
    private ServiceConnection serviceConnection;
    private MyService myService;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBackgroundService();
 
    }

    private void startBackgroundService() {
        Intent backgroundServiceIntent = new Intent(MainActivity.this, MyService.class);
        long waitTime = 5*1000;
        backgroundServiceIntent.putExtra("waitTime", waitTime);
        startService(backgroundServiceIntent);


        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                myService = ((MyService.MyServiceBinder)service).getService();
                Log.d("MainActivity", "service bound");

            }

            public void onServiceDisconnected(ComponentName className) {
                myService = null;
                Log.d("MainActivity", "Service unbound");
            }
        };
        bindService(new Intent(MainActivity.this,
                MyService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        bound = true;
    }
    private void setCurrentWeather(WeatherObject wo) {
        TextView currentDesTV = (TextView) findViewById(R.id.currentDesTV);
        TextView currentTempTV = (TextView) findViewById(R.id.currentTempTV);
        TextView currentTimeTV = (TextView) findViewById(R.id.currentTimeTV);
        TextView currentWindTV = (TextView) findViewById(R.id.currentWindTV);
        currentDesTV.setText(wo.getWeatherDes());
        currentTempTV.setText(wo.getTime());
        currentTimeTV.setText(wo.getTemp() +  " °C");
        currentWindTV.setText(wo.getWindSpeed()+" m/s");
    }


    private void handleBackgroundResult(ArrayList<WeatherObject> listOfWO){
        WeatherObject wo = listOfWO.get(0);
        setCurrentWeather(wo);
        String[] arr = new String[listOfWO.size()-1];
        adapter = new CustomAdapter(this, listOfWO, arr);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setClickable(false);
    }

    private BroadcastReceiver onBackgroundServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MAIN", "Broadcast received from bg service");
            ArrayList<WeatherObject> result = (ArrayList<WeatherObject>) intent.getSerializableExtra("WeatherObject");
            handleBackgroundResult(result);
        }
    };

    @Override
    protected void onResume() {
        Log.d("Main", "registering receivers");

        IntentFilter filter = new IntentFilter();
        filter.addAction("weatherService");

        //can use registerReceiver(...)
        //but using local broadcasts for this service:
        LocalBroadcastManager.getInstance(this).registerReceiver(onBackgroundServiceResult, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {

        Log.d("main", "unregistering receivers, onpause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onBackgroundServiceResult);
        super.onPause();
    }

    public void updateList(View view) {
        myService.currentWeather();
    }
}
