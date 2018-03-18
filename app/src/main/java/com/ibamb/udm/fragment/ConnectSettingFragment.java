package com.ibamb.udm.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.ibamb.udm.R;
import com.ibamb.udm.beans.ChannelParameter;
import com.ibamb.udm.beans.DeviceParameter;
import com.ibamb.udm.beans.TCPChannelParameter;
import com.ibamb.udm.beans.UDPChannelParameter;
import com.ibamb.udm.dto.ParameterTransfer;
import com.ibamb.udm.dto.TCPChannelParameterDTO;
import com.ibamb.udm.instruct.IParameterReaderWriter;
import com.ibamb.udm.instruct.beans.ChannelParamsID;
import com.ibamb.udm.tag.UdmSpinner;

import java.util.ArrayList;
import java.util.List;


public class ConnectSettingFragment extends Fragment {

    private static final String HOST_IP = "IP";
    private static final String HOST_MAC = "MAC";

    private IParameterReaderWriter parameterReaderWriter;
    private View currentView;

    private String ip;
    private String mac;

    private UdmSpinner toSetProtocol;//tup/udp
    private UdmSpinner toSetChannel;
    private UdmSpinner toSetUdpDataMode;
    private Button commitButton;

    private ChannelParameter channelParameter;


    private class ProtoclChangeListener implements Spinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String protocol = "TCP";//(String) protocolSpinner.getItemAtPosition(position);
            AdapterView v = null;
//            if ("TCP".equals(protocol)) {
//                currentView.findViewById(R.id.label_work_as).setVisibility(View.VISIBLE);
//                currentView.findViewById(R.id.id_work_role).setVisibility(View.VISIBLE);
//
//                currentView.findViewById(R.id.label_accepting_income).setVisibility(View.GONE);
//                currentView.findViewById(R.id.id_accepting_income).setVisibility(View.GONE);
//
//                currentView.findViewById(R.id.label_connect_uni_multi).setVisibility(View.GONE);
//                currentView.findViewById(R.id.id_connect_uni_multi).setVisibility(View.GONE);
//            } else if ("UDP".equals(protocol)) {
//                currentView.findViewById(R.id.label_accepting_income).setVisibility(View.VISIBLE);
//                currentView.findViewById(R.id.id_accepting_income).setVisibility(View.VISIBLE);
//
//                currentView.findViewById(R.id.label_connect_uni_multi).setVisibility(View.VISIBLE);
//                currentView.findViewById(R.id.id_connect_uni_multi).setVisibility(View.VISIBLE);
//                currentView.findViewById(R.id.id_work_role).setVisibility(View.GONE);
//                currentView.findViewById(R.id.label_work_as).setVisibility(View.GONE);
//            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class CommitButtonListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {

        }
    }


    public ConnectSettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ip  Parameter 1.
     * @param mac Parameter 2.
     * @return A new instance of fragment ConnectSettingFragment.
     */
    public static ConnectSettingFragment newInstance(String ip, String mac) {
        ConnectSettingFragment fragment = new ConnectSettingFragment();
        Bundle args = new Bundle();
        args.putString(HOST_IP, ip);
        args.putString(HOST_MAC, mac);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ip = getArguments().getString(HOST_IP);
            mac = getArguments().getString(HOST_MAC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_connect_setting, container, false);
        toSetProtocol = (UdmSpinner) currentView.findViewById(R.id.udm_conn_net_protocol_set);
        toSetChannel = (UdmSpinner) currentView.findViewById(R.id.udm_connect_channel_set);
        toSetUdpDataMode = (UdmSpinner) currentView.findViewById(R.id.udm_conn_udp_data_mode);
        commitButton = currentView.findViewById(R.id.id_conect_setting_commit);

        String[] parmaIds = ChannelParamsID.getTcpParamsId("1");// read default channel 1. default protocol tcp.
        ChannelParameter initChannelParam = parameterReaderWriter.readChannelParam("1",parmaIds);
        ParameterTransfer.transTcpParamToView(currentView,initChannelParam);
        initParamView();// init param view element.
        bindParamChangeEvent();// init event.
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelParameter parameter = null;
                if ("TCP".equals(toSetProtocol.getValue())) {
                    parameter = ParameterTransfer.getTcpParamFromView(currentView, mac);
                    parameter = parameterReaderWriter.writeChannelParam(parameter);
                    ParameterTransfer.transTcpParamToView(currentView, parameter);
                } else if ("UDP".equals(toSetProtocol.getValue())) {
                    parameter = ParameterTransfer.getUdpParamFromView(currentView, mac);
                    parameter = parameterReaderWriter.writeChannelParam(parameter);
                    ParameterTransfer.transUdpParamToView(currentView, parameter);
                }
            }
        });
        return currentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IParameterReaderWriter) {
            parameterReaderWriter = (IParameterReaderWriter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parameterReaderWriter = null;
    }

    private void initParamView(){
        //todo
    }
    private void bindParamChangeEvent() {
        /**
         * When TCP or UDP switched , parameters must to reload.
         */
        toSetProtocol.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String[] paramIds = null;
                if (s.toString().equals("TCP")) {
                    paramIds = ChannelParamsID.getTcpParamsId(toSetChannel.getValue());
                } else if (s.toString().equals("UDP")) {
                    paramIds = ChannelParamsID.getTcpParamsId(toSetChannel.getValue());
                }
                channelParameter = parameterReaderWriter.readChannelParam(toSetChannel.getValue(), paramIds);
                if (s.toString().equals("TCP")) {
                    ParameterTransfer.transTcpParamToView(currentView, channelParameter);
                } else if (s.toString().equals("UDP")) {
                    ParameterTransfer.transUdpParamToView(currentView, channelParameter);
                }
            }
        });
        /**
         * When channel changed , parameters must to reload.
         */
        toSetChannel.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String channelId = s.toString();
                String[] paramIds = null;
                if (toSetProtocol.getValue().equals("TCP")) {
                    paramIds = ChannelParamsID.getTcpParamsId(channelId);
                } else if (toSetProtocol.getValue().equals("UDP")) {
                    paramIds = ChannelParamsID.getTcpParamsId(channelId);
                }
                channelParameter = parameterReaderWriter.readChannelParam(channelId, paramIds);
                if (toSetProtocol.getValue().equals("TCP")) {
                    ParameterTransfer.transTcpParamToView(currentView, channelParameter);
                } else if (toSetProtocol.getValue().equals("UDP")) {
                    ParameterTransfer.transUdpParamToView(currentView, channelParameter);
                }
            }
        });
        /**
         * When UDP data mode changed , parameters must to change display.
         */
        toSetUdpDataMode.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

}
