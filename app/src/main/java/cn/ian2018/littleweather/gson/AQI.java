package cn.ian2018.littleweather.gson;

/**
 * Created by Administrator on 2017/2/24/024.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
