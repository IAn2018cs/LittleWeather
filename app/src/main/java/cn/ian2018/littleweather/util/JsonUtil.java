package cn.ian2018.littleweather.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.ian2018.littleweather.db.City;
import cn.ian2018.littleweather.db.County;
import cn.ian2018.littleweather.db.Province;

/**
 * Created by Administrator on 2017/2/24/024.
 */

public class JsonUtil {
    /**
     * 解析省级json数据
     * @param response json数据
     * @return 是否解析成功
     */
    public static boolean handleProvinceResponse(String response){
        if (!response.equals("")) {
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i=0; i < allProvince.length(); i++) {
                    JSONObject provinceObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析市级json数据
     * @param response json数据
     * @return 是否解析成功
     */
    public static boolean handleCityResponse(String response, int provinceId){
        if (!response.equals("")) {
            try {
                JSONArray allCity = new JSONArray(response);
                for (int i=0; i < allCity.length(); i++) {
                    JSONObject cityObject = allCity.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析县级json数据
     * @param response json数据
     * @return 是否解析成功
     */
    public static boolean handleCountyResponse(String response, int cityId){
        if (!response.equals("")) {
            try {
                JSONArray allCounty = new JSONArray(response);
                for (int i=0; i < allCounty.length(); i++) {
                    JSONObject countyObject = allCounty.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
