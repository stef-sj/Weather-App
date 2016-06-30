package com.example.peter.lab7atyourservice;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class WeatherHandler extends AsyncTask<Object,Void,WeatherObject>{

    //Delegate helps us get response in postExecute with DBHandler still being async.
    public IAsyncResponse delegate = null;

    @Override
    protected WeatherObject doInBackground(Object... params) {
        String response = "No Response";
        String url = (String)params[0];
        WeatherObject wo = GETrequest(url);
        return wo;
    }

    @Override
    protected void onPostExecute(WeatherObject wo) {
        delegate.processFinish(wo);
    }

    private WeatherObject GETrequest(String urlStr) {
        WeatherObject wo = null;
        try{
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            wo = getResponse(conn);
        }catch(Exception e){}
        return wo;
    }

    //read response and put it together as a String
    public WeatherObject getResponse(URLConnection conn) {
        WeatherObject wo = null;
        String response = "ERROR IN WEATHERHANDLER";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            response  = sb.toString();
        }catch(Exception e){e.printStackTrace();}
        wo = JsonHandler(response);
        return wo;
    }
    private WeatherObject JsonHandler(String input) {
        WeatherObject wo = null;
        try {
            JSONObject jsonObject = new JSONObject(input);

            JSONArray jsonWeatherArr = jsonObject.getJSONArray("weather");
            JSONObject jsonWind = jsonObject.getJSONObject("wind");
            JSONObject jsonTemp = jsonObject.getJSONObject("main");
            JSONObject jsonWeatherDes = jsonWeatherArr.getJSONObject(0);

            String weatherDes = jsonWeatherDes.getString("description");
            String windSpeed = jsonWind.getString("speed");
            String temp = jsonTemp.getString("temp");
            String city = jsonObject.getString("name");

            Calendar c = Calendar.getInstance();
            String time = c.get(Calendar.HOUR_OF_DAY) + ":"+ c.get(Calendar.MINUTE);

            wo = new WeatherObject(weatherDes, windSpeed, temp, city, time);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wo;
    }
}


