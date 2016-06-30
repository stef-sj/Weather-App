package com.example.peter.lab7atyourservice;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class CustomAdapter extends ArrayAdapter<String> {

    private Activity context;
    private ArrayList<WeatherObject> listOfWO;
    private String[] arr;
    Integer[] imgArr = {
            R.drawable.sun,
            R.drawable.cloud,
            R.drawable.rain,
    };

    public CustomAdapter(Activity context, ArrayList<WeatherObject> listOfWO, String[] arr) {
        super(context, R.layout.activity_listview, arr);
        this.arr = arr;
        this.context = context;
        this.listOfWO = listOfWO;
    }


    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.activity_listview, null, true);

        TextView desTV = (TextView) rowView.findViewById(R.id.desTV);
        TextView tempTV = (TextView) rowView.findViewById(R.id.tempTV);
        TextView windTV = (TextView) rowView.findViewById(R.id.windTV);
        TextView dateTimeTV = (TextView) rowView.findViewById(R.id.dateTimeTV);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);


        WeatherObject wo = listOfWO.get(position+1);

        desTV.setText(wo.getWeatherDes());
        tempTV.setText(wo.getTemp() + " °C");
        windTV.setText(wo.getWindSpeed() + " m/s");
        dateTimeTV.setText(wo.getTime());
        if(wo.getWeatherDes().contains("rain")){
            imageView.setImageResource(imgArr[2]);
        }else if(wo.getWeatherDes().contains("cloud")){
            imageView.setImageResource(imgArr[1]);
        }else{
            imageView.setImageResource(imgArr[1]);
        }
        return rowView;
    };
}