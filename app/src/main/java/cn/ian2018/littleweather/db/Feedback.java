package cn.ian2018.littleweather.db;

import cn.bmob.v3.BmobObject;

/**
 * Created by Administrator on 2017/2/27/027.
 */

public class Feedback extends BmobObject {
    private String info;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
