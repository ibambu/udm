package com.ibamb.udm.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ibamb.udm.R;
import com.ibamb.udm.module.constants.UdmConstants;
import com.ibamb.udm.module.core.TryUser;
import com.ibamb.udm.fragment.BlankFragment;
import com.ibamb.udm.fragment.DeviceSearchListFragment;
import com.ibamb.udm.listener.UdmBottomMenuClickListener;
import com.ibamb.udm.listener.UdmToolbarMenuClickListener;
import com.ibamb.udm.module.security.AECryptStrategy;
import com.ibamb.udm.module.security.ICryptStrategy;
import com.ibamb.udm.task.DeviceSearchAsyncTask;
import com.ibamb.udm.task.UdmInitAsyncTask;
import com.ibamb.udm.util.TaskBarQuiet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * 应用程序主入口
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView tabDeviceList;
    private TextView tabSetting;


    private DeviceSearchListFragment searchListFragment;
    private BlankFragment f2, f3, f4;

    private InetAddress broadcastAddress;

    @Override
    protected void onStart() {
        super.onStart();
        FileInputStream inputStream = null;
        try {
            StringBuilder strbuffer = new StringBuilder();
            inputStream = openFileInput(UdmConstants.TRY_USER_FILE);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strbuffer.append(line);
            }
            ICryptStrategy aes = new AECryptStrategy();
            String content = strbuffer.toString();//aes.decode(strbuffer.toString(), DefualtECryptValue.KEY);
            String[] tryUsers = content.split("&");
            TryUser.setTryUser(tryUsers);

        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }finally {
            if (inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //将ActionBar位置改放Toolbar.
        mToolbar = (Toolbar) findViewById(R.id.udm_toolbar);
        setSupportActionBar(mToolbar);

        //设置右上角的填充菜单
        mToolbar.inflateMenu(R.menu.tool_bar_menu);
        //这句代码使启用Activity回退功能，并显示Toolbar上的左侧回退图标
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //绑定菜单点击事件
        mToolbar.setOnMenuItemClickListener(new UdmToolbarMenuClickListener(this));

        TaskBarQuiet.setStatusBarColor(this, UdmConstants.TASK_BAR_COLOR);//修改任务栏背景颜色

        //默认显示第一个界面
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideAllFragment(transaction);
        if (searchListFragment == null) {
            searchListFragment = DeviceSearchListFragment.newInstance("第一个Fragment", null);
            transaction.add(R.id.fragment_container, searchListFragment);
            transaction.show(searchListFragment);
        } else {
            transaction.show(searchListFragment);

        }
        transaction.commit();
        //底部菜单绑定点击事件,实现界面切换.
        tabDeviceList = (TextView) this.findViewById(R.id.tab_device_list);
        tabSetting = (TextView) this.findViewById(R.id.tab_setting);
        UdmBottomMenuClickListener bottomMenuClickListener = new UdmBottomMenuClickListener(fragmentManager,searchListFragment,
                tabDeviceList,tabSetting);
        tabDeviceList.setOnClickListener(bottomMenuClickListener);
        tabSetting.setOnClickListener(bottomMenuClickListener);
        tabDeviceList.requestFocus();
        tabDeviceList.setSelected(true);
        //初始化应用基础数据
        AssetManager mAssetManger = getAssets();
        UdmInitAsyncTask initAsyncTask = new UdmInitAsyncTask();
        initAsyncTask.execute(mAssetManger);
        //判断WIFI是否开启
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            /**
             * 判断WIFI是否连接,非WIFI网络下不搜索设备.
             */
            String wifiIp = "";
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    int ipAddress = wifiInfo.getIpAddress();
                    wifiIp = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                            + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
                }
            }

        } catch (Exception e) {
            Log.e(this.getClass().getName(),e.getMessage());
        }
    }

    //重置所有文本的选中状态
    public void selected() {
        tabDeviceList.setSelected(false);
        tabSetting.setSelected(false);
    }

    //隐藏所有Fragment
    public void hideAllFragment(FragmentTransaction transaction) {
        if (searchListFragment != null) {
            transaction.hide(searchListFragment);
        }
        if (f2 != null) {
            transaction.hide(f2);
        }
        if (f3 != null) {
            transaction.hide(f3);
        }
        if (f4 != null) {
            transaction.hide(f4);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tool_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==1){
            if(data!=null){
                String searchKewWord = data.getStringExtra("SEARCH_KEY_WORD");
                View view = searchListFragment.getView();
                FloatingActionButton searchButton = view.findViewById(R.id.udm_search_button);
                ListView mListView = (ListView) view.findViewById(R.id.search_device_list);
                TextView vSearchNotice = view.findViewById(R.id.search_notice_info);
                DeviceSearchAsyncTask task = new DeviceSearchAsyncTask(searchButton,mListView,vSearchNotice,
                        searchListFragment.getLayoutInflater());
                task.execute(searchKewWord);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
