package cn.ian2018.littleweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/2/24/024.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    @SerializedName("drsg")
    public DressingAdvice dressingAdvice;

    @SerializedName("flu")
    public Sick sick;

    public class Comfort {
        @SerializedName("txt")
        public String info;
    }

    public class CarWash {
        @SerializedName("txt")
        public String info;
    }

    public class Sport {
        @SerializedName("txt")
        public String info;
    }

    public class DressingAdvice {
        @SerializedName("txt")
        public String info;
    }

    public class Sick {
        @SerializedName("txt")
        public String info;
    }
}
