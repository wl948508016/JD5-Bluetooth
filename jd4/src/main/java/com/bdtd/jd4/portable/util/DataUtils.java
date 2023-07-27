package com.bdtd.jd4.portable.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DataUtils {

    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }

    public static String StrToHex(String text) {
        return StrToHex(text, false);
    }

    public static String StrToHex(String text, boolean isUtf8) {
        //将字符串转为GB2312数组
        byte[] arr = new byte[0];
        try {
            arr = text.getBytes(isUtf8 ? "UTF8" : "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //将数组转为16进制字符串
        String hexStr = "";
        for (int i = 0; i < arr.length; i++) {
            String str = byteToHex(arr[i]);
            hexStr = hexStr + str;
        }
        return hexStr.toUpperCase();
    }

    public static List<String> getIPAddressByRegex(String str) {
        List<String> ips = new ArrayList<>();
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        if (str.matches(regex)) {
            String[] arr = str.split("\\.");
            for (int i = 0; i < 4; i++) {
                ips.add(arr[i]);
            }
        }
        return ips;
    }

    public static String intToBit(int num, int digits) {
        byte byteNum = (byte) num;
        String result = "";
        byte[] array = new byte[digits];
        if (digits == 7) {
            result = result + (byte) ((byteNum >> 6) & 0x1) + (byte) ((byteNum >> 5) & 0x1) + (byte) ((byteNum >> 4) & 0x1);
        }
        if (digits == 6) {
            result = result + (byte) ((byteNum >> 5) & 0x1) + (byte) ((byteNum >> 4) & 0x1);
        }
        if (digits == 5) {
            result = result + (byte) ((byteNum >> 4) & 0x1);
        }
        return result + (byte) ((byteNum >> 3) & 0x1) +
                (byte) ((byteNum >> 2) & 0x1) +
                (byte) ((byteNum >> 1) & 0x1) +
                (byte) ((byteNum) & 0x1);
    }

    public static String getFileAddSpace(String replace) {
        String regex = "(.{2})";
        replace = replace.replaceAll(regex, "$1 ");
        return replace.substring(0, replace.length() - 1);
    }

    public static String getStrHexLength(String str) {
        str = str.replaceAll("[^\\x00-\\xff]", "**");
        int length = str.length();
        return String.format("%02X", length);
    }

    public static String byte2Hex(byte[] b) {
        if ((b == null) || (b.length == 0)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(b.length * 3);
        int size = b.length;
        for (int n = 0; n < size; n++) {
            sb.append("0123456789ABCDEF".charAt(0xF & b[n] >> 4)).append("0123456789ABCDEF".charAt(b[n] & 0xF));
        }
        return sb.toString();
    }

    public static byte[] hex2Bytes(String hexString) {
        byte[] arrB = hexString.getBytes();
        int iLen = arrB.length;
        byte[] arrOut = new byte[iLen / 2];
        String strTmp = null;
        for (int i = 0; i < iLen; i += 2) {
            strTmp = new String(arrB, i, 2);
            arrOut[(i / 2)] = ((byte) Integer.parseInt(strTmp, 16));
        }
        return arrOut;
    }
}