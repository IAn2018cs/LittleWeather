package cn.ian2018.littleweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/2/24/024.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
        @SerializedName("qlty")
        public String airQuality;
    }
}
