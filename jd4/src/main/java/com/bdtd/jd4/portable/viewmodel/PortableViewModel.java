package com.bdtd.jd4.portable.viewmodel;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.bdtd.jd4.portable.util.DataUtils;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;

public class PortableViewModel extends ViewModel {
    private static final String TAG = PortableViewModel.class.getSimpleName();

    /**
     * 将当前时间转换成8位十六进制
     */
    public String formatCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        String second = DataUtils.intToBit(calendar.get(Calendar.SECOND), 6);
        String minute = DataUtils.intToBit(calendar.get(Calendar.MINUTE), 6);
        String hour = DataUtils.intToBit(calendar.get(Calendar.HOUR_OF_DAY), 5);
        String day = DataUtils.intToBit(calendar.get(Calendar.DAY_OF_MONTH), 5);
        String month = DataUtils.intToBit(calendar.get(Calendar.MONTH) + 1, 4);
        String year = DataUtils.intToBit(calendar.get(Calendar.YEAR) - 2020, 6);
        String result = year + month + day + hour + minute + second;
        long decimal = Long.parseLong(result, 2);//将二进制转为十进制
        return String.format("%08X", decimal);//将十进制转为十六进制
    }

    /**
     * 将用户名转换成20位十六进制
     * 不够长补齐“20”
     */
    public String formatUserName(String name) {
        String userName = DataUtils.StrToHex(name);
        StringBuilder nameBuilder = new StringBuilder(userName);
        if (nameBuilder.length() < 16) {
            while (nameBuilder.length() < 16) {
                nameBuilder.append("20");
            }
        }
        return nameBuilder.toString();
    }

    /**
     * 将部门转换成48位十六进制
     * 不够长补齐“20”
     */
    public String formatUserDepartment(String department) {
        String userDepartment = DataUtils.StrToHex(department);
        if (userDepartment.length() < 16) {
            StringBuilder stringBuilder = new StringBuilder(userDepartment);
            if (stringBuilder.length() < 16) {
                while (stringBuilder.length() < 16) {
                    stringBuilder.append("20");
                }
            }
            userDepartment = stringBuilder.toString();
        }
        StringBuilder departmentBuilder = new StringBuilder(userDepartment);
        if (departmentBuilder.length() < 48) {
            while (departmentBuilder.length() < 48) {
                departmentBuilder.append("20");
            }
        }
        return departmentBuilder.toString();
    }

    /**
     * 根据编码格式转换wifi SSID
     */
    public String convertWifiNameToHex(String wifiName, boolean isUtf8) {
        wifiName = DataUtils.StrToHex(wifiName, isUtf8);
        StringBuilder nameBuilder = new StringBuilder(wifiName);
        if (nameBuilder.length() < 64) {
            while (nameBuilder.length() < 64) {
                nameBuilder.append("00");
            }
        }
        return nameBuilder.toString();
    }

    /**
     * 将wifi密码转换成128位十六进制
     */
    public String convertWifiPswToHex(String wifiPsw) {
        if (!TextUtils.isEmpty(wifiPsw)) {
            wifiPsw = DataUtils.StrToHex(wifiPsw);
        }
        StringBuilder nameBuilder = new StringBuilder(wifiPsw);
        if (nameBuilder.length() < 128) {
            while (nameBuilder.length() < 128) {
                nameBuilder.append("00");
            }
        }
        return nameBuilder.toString();
    }

    /**
     * 将用户编号装换成24位十六进制
     */
    public String formatUserNo(String userId) {
        String userNo = DataUtils.StrToHex(userId);
        StringBuilder nameBuilder = new StringBuilder(userNo);
        if (nameBuilder.length() < 24) {
            while (nameBuilder.length() < 24) {
                nameBuilder.append("20");
            }
        }
        return nameBuilder.toString();
    }

    /**
     * 将IP地址转换成8位十六进制
     */
    public String convertIpToHex(String ipAddress) {
        if (TextUtils.isEmpty(ipAddress)) {
            return "00000000";
        }
        List<String> ipArray = DataUtils.getIPAddressByRegex(ipAddress);
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (String ip : ipArray) {
                int address = Integer.parseInt(ip);
                String addressStr = String.format("%02X", address);
                stringBuilder.append(addressStr);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e(TAG, "convertIpToHex: " + e.getMessage());
        }
        return "00000000";
    }

    public boolean checkIpLength(String ip) {
        if (TextUtils.isEmpty(ip)) {
            return true;
        }
        List<String> ipArray = DataUtils.getIPAddressByRegex(ip);
        return ipArray.size() != 4;
    }

    public String formatUploadInterval(String mUploadInterval) {
        int interval = Integer.parseInt(mUploadInterval);
        return String.format("%04X", interval);
    }

    public String formatServerPort(String serverPort) {
        int port = 0;
        try {
            port = Integer.parseInt(serverPort);
        } catch (Exception e) {
            Log.e(TAG, "formatServerPort: " + e.getMessage());
        }
        return String.format("%04X", port);
    }

    /**
     * 将UTF-8格式wifi SSID长度转换成十六进制
     */
    public String formatUTF8SSidLength(String ssid) {
        int length = ssid.getBytes(StandardCharsets.UTF_8).length;
        String lengthStr = DataUtils.intToBit(length, 7);
        lengthStr = 1 + lengthStr;
        long decimal = Long.parseLong(lengthStr, 2);//将二进制转为十进制
        return String.format("%02X", decimal);//将十进制转为十六进制
    }
}
