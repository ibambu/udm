package com.ibamb.udm.task;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ibamb.udm.R;
import com.ibamb.udm.adapter.DeviceUpgradeListAdapter;
import com.ibamb.udm.module.beans.DeviceInfo;
import com.ibamb.udm.module.constants.Constants;
import com.ibamb.udm.module.search.DeviceSearch;

import java.util.ArrayList;

public class SearchUpgradeDeviceAsycTask extends AsyncTask<String, String, ArrayList<DeviceInfo>> {
    private ListView mListView;
    private LayoutInflater inflater;
    private ArrayList<DeviceInfo> deviceList;
    private TextView vSearchNotice;

    @Override
    protected ArrayList<DeviceInfo> doInBackground(String... strings) {
        if (deviceList != null) {
            deviceList.clear();
        }
        String keyword = strings != null && strings.length > 0 ? strings[0] : null;
        publishProgress(Constants.INFO_SEARCH_PROGRESS);
        deviceList = DeviceSearch.searchDevice(keyword);
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }
        try {
            Thread.sleep(450);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        publishProgress(String.valueOf(deviceList.size()));
        return deviceList;
    }

    @Override
    protected void onPostExecute(ArrayList<DeviceInfo> deviceInfos) {
        super.onPostExecute(deviceInfos);
        mListView.setVisibility(View.VISIBLE);
        ListAdapter adapter = new DeviceUpgradeListAdapter(R.layout.device_upgrade_item_layout, inflater, deviceList);
        mListView.setAdapter(adapter);
        vSearchNotice.setVisibility(View.GONE);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        mListView.setVisibility(View.GONE);
        vSearchNotice.setVisibility(View.VISIBLE);
        vSearchNotice.setText(values[0]);
    }

    public SearchUpgradeDeviceAsycTask(ListView mListView,TextView vSearchNotice, LayoutInflater inflater) {
        this.mListView = mListView;
        this.inflater = inflater;
        this.vSearchNotice = vSearchNotice;
    }
}