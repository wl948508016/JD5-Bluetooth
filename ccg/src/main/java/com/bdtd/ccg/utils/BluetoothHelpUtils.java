package com.bdtd.ccg.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.widget.Toast;

import com.bdtd.ccg.model.BluetoothDevModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */

@SuppressLint("MissingPermission")
public class BluetoothHelpUtils {
    private final String TAG = "BluetoothHelpUtils";
    private static volatile BluetoothHelpUtils INSTANCE;

    public static BluetoothHelpUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (BluetoothHelpUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BluetoothHelpUtils();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 根据(字符串转化后的)字节码，获取BCC校验的结果
     * @param data
     * @return
     */
    public static String getBCC(byte[] data) {
        String ret = "";
        byte[] BCC = new byte[1];
        for (byte datum : data) {
            BCC[0] = (byte) (BCC[0] ^ datum);
        }
        String hex = Integer.toHexString(BCC[0] & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        ret += hex.toUpperCase();
        return ret;
    }

    /**
     * 校验JD5发送的数据
     */
    public boolean checkJD5Result(List<String> list) {
        String checkCode = list.get(43) + list.get(44);
        int[] checkArray = new int[43];
        for (int i = 0; i < 43; i++) {
            checkArray[i] = Integer.parseInt(list.get(i), 16);
        }
        int checkResult = crc16FromByteArr(checkArray);
        String checkResultStr = Integer.toHexString(checkResult);
        return checkResultStr.equals(checkCode);
    }

    /**
     * 校验白板返回的数据，并返回校验结果
     */
    public String checkWhiteBoardResult(String returnData) {
        String str01 = returnData.replace("\r\n", ""); // 去掉转义字符
        String before = str01.substring(0, str01.indexOf("*"));
        String end = str01.substring(str01.indexOf("*") + 1);
        if (getBCC(before.getBytes()).equalsIgnoreCase(end)) {
            // 校验成功
            String result = before.substring(before.indexOf(",") + 1);
            switch (result) {
                case "200":
                    return "(200)校验成功";
                case "201":
                    return "(201)解析失败";
                case "202":
                    return "(202)上报失败";
                case "203":
                    return "(203)其它错误";
            }
        } else {
            return "校验失败";
        }
        return "回应数据错误";
    }

    /**
     * 获取已连接的JD5设备
     */
    public List<BluetoothDevModel> getConnectedJD5Devices(BluetoothManager blueToothManager) {
        List<BluetoothDevModel> resultList = new ArrayList<>();
        List<BluetoothDevice> gattDevices = blueToothManager.getConnectedDevices(BluetoothProfile.GATT);
        if (gattDevices != null && gattDevices.size() > 0) {
            for (int i = 0; i < gattDevices.size(); i++) {
                BluetoothDevice dev = gattDevices.get(i);
                if (dev.getName() != null) {
                    if (isJD5Device(dev)) {
                        boolean isConnected = blueToothManager.getConnectionState(dev, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED;
                        if (isConnected) {
                            resultList.add(new BluetoothDevModel(dev, false));
                        }
                    }
                }
            }
        }
        return resultList;
    }

    public void removePairDevice(Context context, BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                try {
                    String name = device.getName();
                    if (name != null && (isJD5Device(device))) { // 只取消JD5和白板设备的配对，不取消手机与其它设备的配对
                        Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "清除已配对设备时出现错误:" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 是否为JD5设备
     */
    public Boolean isJD5Device(BluetoothDevice device) {
        if (device.getName().startsWith("JD5")) {
            return true;
        }
        return false;
    }

    /**
     * CRC16校验
     */
    private static final int BITS_OF_BYTE = 8; // 一个字节占 8位
    private static final int POLYNOMIAL = 0XA001; // 多项式
    private static final int INITIAL_VALUE = 0XFFFF; // CRC寄存器默认初始值
    // 校验码计算器：https://www.23bei.com/tool/59.html
    // GB2312在线转换：http://www.mytju.com/classcode/tools/urlencode_gb2312.asp
    public static int crc16FromByteArr(int[] bytes) {
        int res = INITIAL_VALUE;
        for (int data : bytes) {
            res = res ^ data;
            for (int i = 0; i < BITS_OF_BYTE; i++) {
                res = (res & 0X0001) == 1 ? (res >> 1) ^ POLYNOMIAL : res >> 1;
            }
        }
//        return res; // CRC-16(MSB-LSB)高字节在前，低字节在后
        return revert(res); // CRC-16(Modbus)低字节在前，高字节在后
    }
    private static int revert(int src) {
        int lowByte = (src & 0xFF00) >> 8;
        int highByte = (src & 0x00FF) << 8;
        return lowByte | highByte;
    }
}
