package com.bdtd.jd4.portable.activity;

import android.app.Application;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.bdtd.jd4.R;
import com.bdtd.jd4.databinding.ActivityPortableBinding;
import com.bdtd.jd4.portable.util.DataUtils;
import com.bdtd.jd4.portable.util.TCPClient;
import com.bdtd.jd4.portable.viewmodel.PortableViewModel;

/**
 * 多参数便携仪配置页面
 */
public class PortableActivity extends AppCompatActivity {
    private static final String TAG = PortableActivity.class.getSimpleName();
    private ActivityPortableBinding mPortableBinding;
    private String mFirstWifiSSid = "";
    private String mFirstWifiPsw = "";
    private String mFirstWifiIp = "";
    private String mFirstWifiSSidLength;
    private String mFirstWifiPswLength;
    private String mFirstSubnet;
    private String mFirstGateway;
    private String mFirstDns;
    private String mSecondWifiSSid = "";
    private String mSecondWifiPsw = "";
    private String mSecondWifiIp = "";
    private String mSecondWifiSSidLength;
    private String mSecondWifiPswLength;
    private String mSecondSubnet;
    private String mSecondGateway;
    private String mSecondDns;
    private String mThirdWifiSSid = "";
    private String mThirdWifiPsw = "";
    private String mThirdWifiIp = "";
    private String mThirdWifiSSidLength;
    private String mThirdWifiPswLength;
    private String mThirdSubnet;
    private String mThirdGateway;
    private String mThirdDns;
    private String mSocketAddress;
    private String serverIP;
    private String serverPort;
    private String userName;
    private String userNo;
    private String userDepartment;
    private String mFirstMessage = "01109C48006BD6";
    private String mSecondMessage = "01109CB3004A94";
    private String mUploadInterval;
    private TCPClient mTcpClient;
    private ProgressDialog mProgressDialog;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private PortableViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPortableBinding = DataBindingUtil.setContentView(this, R.layout.activity_portable);
        mViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance((Application) getApplicationContext()).create(PortableViewModel.class);
        initView();
    }

    private void initView() {
        mPortableBinding.ivBack.setOnClickListener(v -> finish());

        mPortableBinding.btnConfig.setOnClickListener(v -> {
            getInputValue();
            checkValue();
        });
    }

    /**
     * 配置消息分两次发送，发送完第一段指令后，收到便携仪指令后发送第二段
     */
    private void startTcpClient() {
        mTcpClient = new TCPClient(mSocketAddress, 6666, new TCPClient.ReceiverMsgListener() {
            @Override
            public void receiverMsg(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Log.i(TAG, "receive message: " + msg);
                    if (msg.contains("01109C4800772E69")) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "第一条指令下发成功！", Toast.LENGTH_SHORT).show());
                        mHandler.postDelayed(() -> mTcpClient.send(mSecondMessage), 5 * 1000L);
                    } else if (msg.contains("01109CBF0050DF81")) {
                        mTcpClient.close();
                        runOnUiThread(() -> {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "配置成功！", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        mTcpClient.close();
                        runOnUiThread(() -> {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "配置失败！" + msg, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void connectSuccess() {
                if (mTcpClient.isConnected()) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "连接成功！", Toast.LENGTH_SHORT).show());
                    mHandler.postDelayed(() -> mTcpClient.send(mFirstMessage), 500L);
                }
            }

            @Override
            public void connectFail(String errorMsg) {
                mTcpClient.close();
                mProgressDialog.dismiss();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "连接失败！" + errorMsg, Toast.LENGTH_SHORT).show());
            }
        });

        mTcpClient.connect();
        mProgressDialog = new ProgressDialog(PortableActivity.this);
        mProgressDialog.setMessage("便携仪信息配置中！");
        mProgressDialog.show();
    }


    private void formatValue() {
        String currentTime = mViewModel.formatCurrentTime();
        userName = mViewModel.formatUserName(userName);
        userNo = mViewModel.formatUserNo(userNo);
        userDepartment = mViewModel.formatUserDepartment(userDepartment);
        formatWifiName();
        formatWifiPsw();
        formatServerIP();
        formatSubnet();
        formatGateway();
        formatDns();
        serverPort = mViewModel.formatServerPort(serverPort);
        mUploadInterval = mViewModel.formatUploadInterval(mUploadInterval);

        mFirstMessage = "01109C480077EE";
        mSecondMessage = "01109CBF0050A0";
        mFirstMessage = mFirstMessage
                + currentTime
                + serverIP
                + serverPort
                + mFirstWifiSSidLength
                + mFirstWifiPswLength
                + mFirstWifiSSid
                + mFirstWifiPsw
                + mFirstWifiIp
                + mFirstSubnet
                + mFirstGateway
                + mFirstDns
                + mSecondWifiSSidLength
                + mSecondWifiPswLength
                + mSecondWifiSSid
                + mSecondWifiPsw
                + mSecondWifiIp
                + mSecondSubnet
                + mSecondGateway
                + mSecondDns
                + "D484";
        Log.e(TAG, "formatValue mFirstMessage: " + DataUtils.getFileAddSpace(mFirstMessage));

        mSecondMessage = mSecondMessage
                + mThirdWifiSSidLength
                + mThirdWifiPswLength
                + mThirdWifiSSid
                + mThirdWifiPsw
                + mThirdWifiIp
                + mThirdSubnet
                + mThirdGateway
                + mThirdDns
                + userName
                + userNo
                + userDepartment
                + mUploadInterval
                + "B534";
        Log.e(TAG, "formatValue mSecondMessage: " + DataUtils.getFileAddSpace(mSecondMessage));
        startTcpClient();
    }

    private void formatWifiName() {
        int firstSsidEncode = mPortableBinding.rgFirstSsidEncode.getCheckedRadioButtonId() == R.id.rb_first_encode_gkb ? 0 : 1;
        if (firstSsidEncode == 0) {
            mFirstWifiSSidLength = DataUtils.getStrHexLength(mFirstWifiSSid);
            mFirstWifiSSid = mViewModel.convertWifiNameToHex(mFirstWifiSSid, false);
        } else {
            mFirstWifiSSidLength = mViewModel.formatUTF8SSidLength(mFirstWifiSSid);
            mFirstWifiSSid = mViewModel.convertWifiNameToHex(mFirstWifiSSid, true);
        }

        int secondSsidEncode = mPortableBinding.rgSecondSsidEncode.getCheckedRadioButtonId() == R.id.rb_second_encode_gkb ? 0 : 1;
        if (secondSsidEncode == 0) {
            mSecondWifiSSidLength = DataUtils.getStrHexLength(mSecondWifiSSid);
            mSecondWifiSSid = mViewModel.convertWifiNameToHex(mSecondWifiSSid, false);
        } else {
            mSecondWifiSSidLength = mViewModel.formatUTF8SSidLength(mSecondWifiSSid);
            mSecondWifiSSid = mViewModel.convertWifiNameToHex(mSecondWifiSSid, true);
        }

        int thirdSsidEncode = mPortableBinding.rgThirdSsidEncode.getCheckedRadioButtonId() == R.id.rb_third_encode_gkb ? 0 : 1;
        if (thirdSsidEncode == 0) {
            mThirdWifiSSidLength = DataUtils.getStrHexLength(mThirdWifiSSid);
            mThirdWifiSSid = mViewModel.convertWifiNameToHex(mThirdWifiSSid, false);
        } else {
            mThirdWifiSSidLength = mViewModel.formatUTF8SSidLength(mThirdWifiSSid);
            mThirdWifiSSid = mViewModel.convertWifiNameToHex(mThirdWifiSSid, true);
        }
    }

    private void formatWifiPsw() {
        mFirstWifiPswLength = DataUtils.getStrHexLength(mFirstWifiPsw);
        mFirstWifiPsw = mViewModel.convertWifiPswToHex(mFirstWifiPsw);
        mSecondWifiPswLength = DataUtils.getStrHexLength(mSecondWifiPsw);
        mSecondWifiPsw = mViewModel.convertWifiPswToHex(mSecondWifiPsw);
        mThirdWifiPswLength = DataUtils.getStrHexLength(mThirdWifiPsw);
        mThirdWifiPsw = mViewModel.convertWifiPswToHex(mThirdWifiPsw);
    }

    private void formatServerIP() {
        serverIP = mViewModel.convertIpToHex(serverIP);
        mFirstWifiIp = mViewModel.convertIpToHex(mFirstWifiIp);
        mSecondWifiIp = mViewModel.convertIpToHex(mSecondWifiIp);
        mThirdWifiIp = mViewModel.convertIpToHex(mThirdWifiIp);
    }

    private void formatSubnet() {
        mFirstSubnet = mViewModel.convertIpToHex(mFirstSubnet);
        mSecondSubnet = mViewModel.convertIpToHex(mSecondSubnet);
        mThirdSubnet = mViewModel.convertIpToHex(mThirdSubnet);
    }

    private void formatGateway() {
        mFirstGateway = mViewModel.convertIpToHex(mFirstGateway);
        mSecondGateway = mViewModel.convertIpToHex(mSecondGateway);
        mThirdGateway = mViewModel.convertIpToHex(mThirdGateway);
    }

    private void formatDns() {
        mFirstDns = mViewModel.convertIpToHex(mFirstDns);
        mSecondDns = mViewModel.convertIpToHex(mSecondDns);
        mThirdDns = mViewModel.convertIpToHex(mThirdDns);
    }

    private void getInputValue() {
        mFirstWifiSSid = mPortableBinding.etFirstWifiSsid.getText().toString().trim();
        mFirstWifiPsw = mPortableBinding.etFirstWifiPsw.getText().toString().trim();
        mSecondWifiSSid = mPortableBinding.etSecondWifiSsid.getText().toString().trim();
        mSecondWifiPsw = mPortableBinding.etSecondWifiPsw.getText().toString().trim();
        mThirdWifiSSid = mPortableBinding.etThirdWifiSsid.getText().toString().trim();
        mThirdWifiPsw = mPortableBinding.etThirdWifiPsw.getText().toString().trim();
        mSocketAddress = mPortableBinding.etDeviceIp.getText().toString().trim();
        serverIP = mPortableBinding.etServerIp.getText().toString().trim();
        serverPort = mPortableBinding.etServerPort.getText().toString().trim();
        userName = mPortableBinding.etUserName.getText().toString().trim();
        userNo = mPortableBinding.etUserNo.getText().toString().trim();
        userDepartment = mPortableBinding.etUserDepartment.getText().toString().trim();
        mUploadInterval = mPortableBinding.etUploadInterval.getText().toString().trim();
        mFirstWifiIp = mPortableBinding.etFirstWifiIp.getText().toString().trim();
        mSecondWifiIp = mPortableBinding.etSecondWifiIp.getText().toString().trim();
        mThirdWifiIp = mPortableBinding.etThirdWifiIp.getText().toString().trim();
        mFirstSubnet = mPortableBinding.etFirstSubnet.getText().toString().trim();
        mFirstGateway = mPortableBinding.etFirstGetaway.getText().toString().trim();
        mFirstDns = mPortableBinding.etFirstDns.getText().toString().trim();
        mSecondSubnet = mPortableBinding.etSecondSubnet.getText().toString().trim();
        mSecondGateway = mPortableBinding.etSecondGetaway.getText().toString().trim();
        mSecondDns = mPortableBinding.etSecondDns.getText().toString().trim();
        mThirdSubnet = mPortableBinding.etThirdSubnet.getText().toString().trim();
        mThirdGateway = mPortableBinding.etThirdGetaway.getText().toString().trim();
        mThirdDns = mPortableBinding.etThirdDns.getText().toString().trim();
    }

    private void checkValue() {
        if (TextUtils.isEmpty(mFirstWifiSSid)) {
            showMessage("请输入WIFI SSID");
            return;
        }
        if (TextUtils.isEmpty(serverIP) || mViewModel.checkIpLength(serverIP)) {
            showMessage("请输入正确上传地址IP");
            return;
        }
        if (TextUtils.isEmpty(serverPort)) {
            showMessage("请输入上传地址端口号");
            return;
        }
        if (TextUtils.isEmpty(userName)) {
            showMessage("请输入用户姓名");
            return;
        }
        if (TextUtils.isEmpty(userNo)) {
            showMessage("请输入用户编号");
            return;
        }
        if (TextUtils.isEmpty(userDepartment)) {
            showMessage("请输入用户部门");
            return;
        }
        if (TextUtils.isEmpty(mSocketAddress)) {
            showMessage("请输入便携仪地址");
            return;
        }
        if (TextUtils.isEmpty(mUploadInterval)) {
            mUploadInterval = "15";
        }

        int interval = Integer.parseInt(mUploadInterval);
        if (interval > 6000) {
            showMessage("上传时间间隔不能大于1小时!");
            return;
        }
        if (interval < 15) {
            showMessage("上传时间间隔不能小于15秒!");
            return;
        }

        if (!TextUtils.isEmpty(mFirstWifiIp) && mViewModel.checkIpLength(mFirstWifiIp)) {
            showMessage("请输入正确的第一目标WiFi IP");
            return;
        }
        if (!TextUtils.isEmpty(mSecondWifiIp) && mViewModel.checkIpLength(mSecondWifiIp)) {
            showMessage("请输入正确的第二目标WiFi IP");
            return;
        }
        if (!TextUtils.isEmpty(mThirdWifiIp) && mViewModel.checkIpLength(mThirdWifiIp)) {
            showMessage("请输入正确的第三目标WiFi IP");
            return;
        }
        if (!TextUtils.isEmpty(mFirstSubnet) && mViewModel.checkIpLength(mFirstSubnet)) {
            showMessage("请输入正确的第一目标WiFi子网掩码");
            return;
        }
        if (!TextUtils.isEmpty(mSecondSubnet) && mViewModel.checkIpLength(mSecondSubnet)) {
            showMessage("请输入正确的第二目标WiFi子网掩码");
            return;
        }
        if (!TextUtils.isEmpty(mThirdSubnet) && mViewModel.checkIpLength(mThirdSubnet)) {
            showMessage("请输入正确的第三目标WiFi子网掩码");
            return;
        }
        if (!TextUtils.isEmpty(mFirstGateway) && mViewModel.checkIpLength(mFirstGateway)) {
            showMessage("请输入正确的第一目标WiFi网关");
            return;
        }
        if (!TextUtils.isEmpty(mSecondGateway) && mViewModel.checkIpLength(mSecondGateway)) {
            showMessage("请输入正确的第二目标WiFi网关");
            return;
        }
        if (!TextUtils.isEmpty(mThirdGateway) && mViewModel.checkIpLength(mThirdGateway)) {
            showMessage("请输入正确的第三目标WiFi网关");
            return;
        }
        if (!TextUtils.isEmpty(mFirstDns) && mViewModel.checkIpLength(mFirstDns)) {
            showMessage("请输入正确的第一目标WiFi DNS");
            return;
        }
        if (!TextUtils.isEmpty(mSecondDns) && mViewModel.checkIpLength(mSecondDns)) {
            showMessage("请输入正确的第二目标WiFi DNS");
            return;
        }
        if (!TextUtils.isEmpty(mThirdDns) && mViewModel.checkIpLength(mThirdDns)) {
            showMessage("请输入正确的第三目标WiFi DNS");
            return;
        }
        formatValue();
    }

    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (mTcpClient != null) {
            mTcpClient.close();
        }
        super.onDestroy();
    }
}