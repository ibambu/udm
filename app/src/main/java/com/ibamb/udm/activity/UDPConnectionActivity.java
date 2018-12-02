package com.ibamb.udm.activity;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibamb.udm.R;
import com.ibamb.udm.component.constants.UdmConstant;
import com.ibamb.udm.listener.UdmGestureListener;
import com.ibamb.dnet.module.log.UdmLog;
import com.ibamb.dnet.module.beans.DeviceParameter;
import com.ibamb.dnet.module.beans.ParameterItem;
import com.ibamb.dnet.module.core.ParameterMapping;
import com.ibamb.dnet.module.instruct.beans.Parameter;
import com.ibamb.udm.listener.UdmReloadParamsClickListener;
import com.ibamb.dnet.module.net.IPUtil;
import com.ibamb.udm.task.ChannelParamReadAsyncTask;
import com.ibamb.udm.task.ChannelParamWriteAsynTask;
import com.ibamb.udm.util.TaskBarQuiet;
import com.ibamb.udm.util.ViewElementDataUtil;

import java.util.ArrayList;
import java.util.List;

public class UDPConnectionActivity extends AppCompatActivity implements View.OnTouchListener {
    private DeviceParameter deviceParameter;
    private View currentView;
    private String mac;
    private String channelId;
    private String ip;
    private boolean isCanDNS;

    private ImageView commit;
    private ImageView back;
    private TextView title;

    private static final String[] UDP_SETTING_PARAMS_TAG = {
            "CONN_UDP_DATA_MODE",
            "CONN_UDP_TMP_HOST_EN",
            "CONN_UDP_UNI_HOST_IP0",
            "CONN_UDP_MUL_LOCAL_PORT",
            "CONN_UDP_MUL_REMOTE_IP",
            "CONN_UDP_ACCEPTION",
            "CONN_UDP_UNI_LOCAL_PORT",
            "CONN_UDP_UNI_HOST_PORT0",
            "CONN_UDP_MUL_REMOTE_PORT"};

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udpconnection);
        TaskBarQuiet.setStatusBarColor(this, UdmConstant.TASK_BAR_COLOR);

        getSupportActionBar();
        Bundle bundle = getIntent().getExtras();
        mac = bundle.getString("HOST_MAC");
        ip = bundle.getString("HOST_ADDRESS");
        channelId = bundle.getString("CHNL_ID");
        isCanDNS = bundle.getBoolean("IS_CAN_DNS");
        currentView = getWindow().getDecorView();

        commit = findViewById(R.id.do_commit);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int errId = checkData();
                if (errId == 0) {
                    DeviceParameter changedParam = ViewElementDataUtil.getChangedData(currentView, deviceParameter, channelId);
                    ChannelParamWriteAsynTask task = new ChannelParamWriteAsynTask(currentView);
                    task.execute(deviceParameter, changedParam);
                } else {
                    EditText editText = findViewById(errId);
                    editText.requestFocus();
                    String notice = editText.getText().toString() + " is invalid.";
                    Snackbar.make(editText, notice, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

        back = findViewById(R.id.go_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        title = findViewById(R.id.title);
        title.setText(UdmConstant.TITLE_UDP_CONNECTION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            deviceParameter = new DeviceParameter(mac, ip, channelId);
            List<Parameter> parameters = ParameterMapping.getInstance().getMappingByTags(UDP_SETTING_PARAMS_TAG, channelId);
            List<ParameterItem> items = new ArrayList<>();
            for (Parameter parameter : parameters) {
                items.add(new ParameterItem(parameter.getId(), null));
            }
            deviceParameter.setParamItems(items);
            //点击重新读取参数值，并刷新界面。
            title.setOnClickListener(new UdmReloadParamsClickListener(this, currentView, deviceParameter));
            ChannelParamReadAsyncTask readerAsyncTask = new ChannelParamReadAsyncTask(this, currentView, deviceParameter);
            readerAsyncTask.execute(mac);

            /**
             * 监听手势
             */
            UdmGestureListener listener = new UdmGestureListener(this, deviceParameter, currentView);
            mGestureDetector = new GestureDetector(this, listener);
            findViewById(R.id.v_gesture).setOnTouchListener(this);
            findViewById(R.id.id_conn_uni_local_port).setOnTouchListener(this);
            findViewById(R.id.udm_conn_uni_local_port).setOnTouchListener(this);
            findViewById(R.id.id_conn_uni_host_port0).setOnTouchListener(this);
            findViewById(R.id.id_conn_host_port0).setOnTouchListener(this);
            findViewById(R.id.id_conn_udp_uni_host_ip0).setOnTouchListener(this);
            findViewById(R.id.id_conn_mul_local_port).setOnTouchListener(this);
            findViewById(R.id.udm_conn_mul_local_port).setOnTouchListener(this);
            findViewById(R.id.id_conn_mul_host_port0).setOnTouchListener(this);
            findViewById(R.id.udm_conn_host_port0).setOnTouchListener(this);
            findViewById(R.id.id_conn_udp_mul_remote_ip).setOnTouchListener(this);
            findViewById(R.id.udm_conn_udp_mul_remote_ip).setOnTouchListener(this);

        } catch (Exception e) {
            UdmLog.error(e);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    private int checkData() {
        EditText remoteHost = ((EditText) findViewById(R.id.id_conn_udp_uni_host_ip0));
        EditText multiAddress = ((EditText) findViewById(R.id.udm_conn_udp_mul_remote_ip));
        if (isCanDNS) {
            if (!IPUtil.isDomain(remoteHost.getText().toString())
                    &&!IPUtil.isIPAddress(remoteHost.getText().toString())) {
                return R.id.id_conn_udp_uni_host_ip0;
            }
        } else if (!IPUtil.isIPAddress(remoteHost.getText().toString())) {
            return R.id.id_conn_udp_uni_host_ip0;
        }

        if (!IPUtil.isIPAddress(multiAddress.getText().toString())) {
            return R.id.udm_conn_udp_mul_remote_ip;
        }
        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
}
