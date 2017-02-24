package cn.ian2018.littleweather.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.ian2018.littleweather.R;
import cn.ian2018.littleweather.activity.MainActivity;
import cn.ian2018.littleweather.activity.WeatherActivity;
import cn.ian2018.littleweather.db.City;
import cn.ian2018.littleweather.db.County;
import cn.ian2018.littleweather.db.Province;
import cn.ian2018.littleweather.util.Constant;
import cn.ian2018.littleweather.util.HttpUtil;
import cn.ian2018.littleweather.util.JsonUtil;
import cn.ian2018.littleweather.util.Logs;
import cn.ian2018.littleweather.util.SharedPreferencesUtil;
import cn.ian2018.littleweather.util.ToastUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/2/24/024.
 * 选择城市
 */

public class ChooseFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    // 当前选中的级别
    private int currentLevel;
    // 各级列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    // 选中的省
    private Province selectedProvince;
    // 选中的市
    private City selectedCity;

    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private List<String> dataList = new ArrayList<>();
    private MyListAdapter adapter;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);

        adapter = new MyListAdapter();
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 首先查询省级信息
        queryProvince();
        // 设置listView点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCounty();
                } else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(i).getWeatherId();
                    SharedPreferencesUtil.putString("weather_id", weatherId);
                    // 如果当前activity属于MainActivity
                    if (getActivity() instanceof MainActivity) {
                        // 跳转页面
                        startActivity(new Intent(getActivity(), WeatherActivity.class));
                        getActivity().finish();
                    // 如果当前activity属于WeatherActivity
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.swipeRefresh.setRefreshing(true);
                        weatherActivity.requestWeather(weatherId);
                    }
                }
            }
        });
        // 设置返回按钮的点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_CITY) {
                    queryProvince();
                } else if (currentLevel == LEVEL_COUNTY) {
                    queryCity();
                }
            }
        });
    }

    /**
     * 查询所有省的信息
     */
    private void queryProvince() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        Logs.d("" + provinceList.size());
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            // 更新数据适配器
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            // 把当前的级别置为省级
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = Constant.CHINA_ADDRESS_URL;
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询所有省下的城市
     */
    private void queryCity() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            // 更新数据适配器
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            // 把当前的级别置为省级
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = Constant.CHINA_ADDRESS_URL + "/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询所有市下的县
     */
    private void queryCounty() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            // 更新数据适配器
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            // 把当前的级别置为省级
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = Constant.CHINA_ADDRESS_URL + "/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 从服务器查询
     *
     * @param address 接口地址
     * @param type    查询的类型
     */
    private void queryFromServer(String address, final String type) {
        // 显示进度对话框
        showProgressDialog();
        // 发送请求
        HttpUtil.sendHttpRequest(address, new Callback() {
            // 请求失败
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show("加载失败");
                        closeProgressDialog();
                    }
                });
            }

            // 请求成功
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                // 是否解析成功
                boolean result = false;
                // 根据类型 解析json数据
                if (type.equals("province")) {
                    result = JsonUtil.handleProvinceResponse(responseText);
                } else if (type.equals("city")) {
                    result = JsonUtil.handleCityResponse(responseText, selectedProvince.getId());
                } else if (type.equals("county")) {
                    result = JsonUtil.handleCountyResponse(responseText, selectedCity.getId());
                }

                Logs.d("" + result);

                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (type.equals("province")) {
                                queryProvince();
                            } else if (type.equals("city")) {
                                queryCity();
                            } else if (type.equals("county")) {
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog != null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    // 数据适配器
    class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public String getItem(int i) {
            return dataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_list_local, viewGroup, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.name_text);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.name.setText(dataList.get(i));

            return view;
        }

        class ViewHolder {
            TextView name;
        }
    }

}
