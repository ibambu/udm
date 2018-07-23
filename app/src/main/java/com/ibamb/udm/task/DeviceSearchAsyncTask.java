package com.ibamb.udm.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibamb.udm.R;
import com.ibamb.udm.adapter.SearchDeviceListPagerAdapter;
import com.ibamb.udm.fragment.DeviceSearchListFragment;
import com.ibamb.udm.module.beans.Device;
import com.ibamb.udm.module.beans.DeviceInfo;
import com.ibamb.udm.module.constants.Constants;
import com.ibamb.udm.module.search.DeviceSearch;

import java.util.ArrayList;
import java.util.List;


public class DeviceSearchAsyncTask extends AsyncTask<String, String, ArrayList<DeviceInfo>> {

    private Activity activity;
    private FragmentManager supportFragmentManager;
    private ArrayList<DeviceInfo> deviceList;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setSupportFragmentManager(FragmentManager supportFragmentManager) {
        this.supportFragmentManager = supportFragmentManager;
    }

    /**
     * 后台搜索设备（工作线程执行）
     *
     * @param strings
     * @return
     */
    @Override
    protected ArrayList<DeviceInfo> doInBackground(String... strings) {
        if (deviceList != null) {
            deviceList.clear();
        }

        String keyword = strings != null && strings.length > 0 ? strings[0] : null;
        publishProgress(Constants.INFO_SEARCH_PROGRESS);
        deviceList = DeviceSearch.searchDevice(keyword);
        if (deviceList == null) {
            int tryMaxCount = 3;
            for (int i = tryMaxCount; i > 0; i--) {
                deviceList = DeviceSearch.searchDevice(keyword);
                if (deviceList != null && !deviceList.isEmpty()) {
                    break;
                }
            }
            if(deviceList== null ){
                deviceList = new ArrayList<>();
            }
        }

        return deviceList;
    }

    /**
     * 将搜索到的设备更新界面列表（主线程执行）
     *
     * @param dataList
     */
    @Override
    protected void onPostExecute(ArrayList<DeviceInfo> dataList) {
        super.onPostExecute(dataList);

        int maxRows = 50;

        List<String> titles = new ArrayList<>();
        List<Fragment> fragmentList = new ArrayList<>();
        View mainView = activity.getWindow().getDecorView();
        TabLayout tabLayout = mainView.findViewById(R.id.device_list_tab);
        ViewPager viewPager = mainView.findViewById(R.id.device_list_pager);

        int fromIndex =0;
        int endIndex = 0;
        int pageCount = 0;
        List<Device> allDeviceList = new ArrayList<>();
        for(DeviceInfo deviceInfo :dataList){
            Device device = deviceInfo.toDevice();
            allDeviceList.add(device);
        }
        while(fromIndex < dataList.size()) {
            if(endIndex+maxRows>dataList.size()){
                endIndex = dataList.size();
            }else{
                endIndex +=maxRows;
            }
            pageCount++;
            List<Device> onePageData = allDeviceList.subList(fromIndex,endIndex);
            fromIndex += maxRows;
            String page = String.valueOf(pageCount);
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(page);
            tabLayout.addTab(tab);
            titles.add(page);
            StringBuilder deviceInfoBuffer = new StringBuilder();
            for(Device device:onePageData){
                deviceInfoBuffer.append(device.toString()).append("@");
            }
            if(deviceInfoBuffer.length()>0){
                deviceInfoBuffer.deleteCharAt(deviceInfoBuffer.length()-1);
            }

            fragmentList.add(DeviceSearchListFragment.newInstance(deviceInfoBuffer.toString()));
        }
        if(pageCount<2){
            tabLayout.setVisibility(View.GONE);
        }else{
            tabLayout.setVisibility(View.VISIBLE);
        }
        if(pageCount<6){
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        }else{
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
        tabLayout.setupWithViewPager(viewPager);
        SearchDeviceListPagerAdapter adapter = new SearchDeviceListPagerAdapter(supportFragmentManager,titles,fragmentList);
        viewPager.setAdapter(adapter);



        String notice = "";
        if (dataList.size() == 0) {
            notice = Constants.INFO_SEARCH_FAIL;
        } else {
            notice = "Found Device:" + String.valueOf(dataList.size());
        }
        Toast.makeText(activity, notice, Toast.LENGTH_SHORT).show();
        TextView vDeviceList = mainView.findViewById(R.id.tab_device_list);
        if(dataList.size()>0){
            vDeviceList.setText("Device List("+dataList.size()+")");
        }
//        vSearchNotice.setVisibility(View.GONE);
    }

    /**
     * 搜索进度
     *
     * @param values
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    public DeviceSearchAsyncTask(ListView mListView, TextView vSearchNotice, LayoutInflater inflater) {

    }
}
