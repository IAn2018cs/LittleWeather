package cn.ian2018.littleweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/2/24/024.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public Wind wind;

    public class More {
        public String code;
        @SerializedName("txt")
        public String info;
    }

    public class Wind {
        @SerializedName("dir")
        public String info;
    }
}
