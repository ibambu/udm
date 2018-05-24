package com.ibamb.udm.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ibamb.udm.R;
import com.ibamb.udm.beans.ChannelParameter;
import com.ibamb.udm.beans.ParameterItem;
import com.ibamb.udm.constants.UdmConstants;
import com.ibamb.udm.core.ParameterMapping;
import com.ibamb.udm.instruct.beans.Parameter;
import com.ibamb.udm.listener.UdmReloadParamsClickListener;
import com.ibamb.udm.net.IPUtil;
import com.ibamb.udm.task.ChannelParamReadAsyncTask;
import com.ibamb.udm.task.ChannelParamWriteAsynTask;
import com.ibamb.udm.util.TaskBarQuiet;
import com.ibamb.udm.util.ViewElementDataUtil;

import java.util.ArrayList;
import java.util.List;

public class IPSettingActivity extends AppCompatActivity {

    private ChannelParameter channelParameter;
    private String mac;
    private String ip;
    private View currentView;

    private TextView commit;
    private TextView back;
    private TextView title;

    private static final String[] IP_SETTING_PARAMS_TAG = {"ETH_AUTO_OBTAIN_IP", "ETH_IP_ADDR",
            "PREFERRED_DNS_SERVER", "ETH_NETMASK_ADDR", "PREFERRED_DNS_SERVER", "ALTERNATE_DNS_SERVE", "ETH_GATEWAY_ADDR"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipsetting);
        TaskBarQuiet.setStatusBarColor(this, UdmConstants.TASK_BAR_COLOR);

        Bundle bundle = getIntent().getExtras();
        mac = bundle.getString("HOST_MAC");
        ip = bundle.getString("HOST_ADDRESS");
        currentView = getWindow().getDecorView();

        commit = findViewById(R.id.do_commit);
        back = findViewById(R.id.go_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int errId = checkData();
                if(errId==0){
                    ChannelParameter changedParam = ViewElementDataUtil.getChangedData(currentView,channelParameter,UdmConstants.UDM_IP_SETTING_CHNL);
                    ChannelParamWriteAsynTask task = new ChannelParamWriteAsynTask(currentView);
                    task.execute(channelParameter,changedParam);
                }else{
                    EditText editText = findViewById(errId);
                    editText.requestFocus();
                    String notice = editText.getText().toString()+" is invalid.";
                    Snackbar.make(v,  notice, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        title=findViewById(R.id.title);
        title.setText("IP Setting");


    }

    @Override
    protected void onStart() {
        super.onStart();
        //在界面初始化完毕后读取默认通道的参数，并且刷新界面数据。
        try {
            channelParameter = new ChannelParameter(mac, UdmConstants.UDM_IP_SETTING_CHNL,ip);
            List<Parameter> parameters = ParameterMapping.getMappingByTags(IP_SETTING_PARAMS_TAG, UdmConstants.UDM_IP_SETTING_CHNL);
            List<ParameterItem> items = new ArrayList<>();
            for (Parameter parameter : parameters) {
                items.add(new ParameterItem(parameter.getId(), null));
            }
            channelParameter.setParamItems(items);
            //点击重新读取参数值，并刷新界面。注意顺序，一定要在channelParameter赋值之后再绑定事件。
            title.setOnClickListener(new UdmReloadParamsClickListener(currentView,channelParameter));
            //后台异步读取参数值并更新界面数据。
            ChannelParamReadAsyncTask readerAsyncTask = new ChannelParamReadAsyncTask(currentView, channelParameter);
            readerAsyncTask.execute(mac);
        } catch (Exception e) {
            Log.e(AccessSettingActivity.class.getName(), e.getMessage());
        }

    }

    private int checkData(){
        EditText ethIpAddress = ((EditText)findViewById(R.id.id_address));
        EditText subnet = ((EditText)findViewById(R.id.id_subnet));
        EditText gateway = ((EditText)findViewById(R.id.id_getway));
        EditText dns1 = ((EditText)findViewById(R.id.id_dns_1));
        EditText dns2 = ((EditText)findViewById(R.id.id_dns_2));

        if(!IPUtil.isIPAddress(ethIpAddress.getText().toString())){
           return R.id.id_address;
        }
        if(!IPUtil.isIPAddress(subnet.getText().toString())){
            return R.id.id_subnet;
        }
        if(!IPUtil.isIPAddress(gateway.getText().toString())){
            return R.id.id_getway;
        }
        if(!IPUtil.isIPAddress(dns1.getText().toString())){
            return R.id.id_dns_1;
        }
        if(!IPUtil.isIPAddress(dns2.getText().toString())){
            return R.id.id_dns_2;
        }
        return 0;
    }

}
