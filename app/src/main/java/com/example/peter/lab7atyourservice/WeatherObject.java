package com.example.peter.lab7atyourservice;

import java.io.Serializable;

/**
 * Created by Peter on 05/05/16.
 */
public class WeatherObject implements Serializable {
    private final String weatherDes;
    private final String windSpeed;
    private final String temp;
    private final String city;
    private final String time;

    public WeatherObject(String weatherDes, String windSpeed, String temp, String city, String time) {
        this.weatherDes = weatherDes;
        this.windSpeed = windSpeed;
        this.temp = temp;
        this.city = city;
        this.time = time;
    }

    public String getWeatherDes() {
        return weatherDes;
    }
    public String getWindSpeed() {
        return windSpeed;
    }
    public String getTemp() {
        return temp;
    }
    public String getCity() {
        return weatherDes;
    }
    public String getTime() {
        return time;
    }
}
