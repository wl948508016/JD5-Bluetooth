package com.bdtd.ccg.common;

public class HexUtil {

    /**
     * 用于建立十六进制字符的输出的小写字符数组
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 用于建立十六进制字符的输出的大写字符数组
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final String HEX_PREFIX = "0x";

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data byte[]
     * @return 十六进制char[]
     */
    public static char[] byteToHex(byte[] data) {
        return byteToHex(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data        byte[]
     * @param toUpperCase <code>true</code> 传换成大写格式 ， <code>false</code> 传换成小写格式
     * @return 十六进制char[]
     */
    public static char[] byteToHex(byte[] data, boolean toUpperCase) {
        return byteToHex(data, toUpperCase ? DIGITS_UPPER : DIGITS_LOWER);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制char[]
     */
    protected static char[] byteToHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.  
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data byte[]
     * @return 十六进制String
     */
    public static String byteToHexStr(byte[] data) {
        return byteToHexStr(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data        byte[]
     * @param toUpperCase <code>true</code> 传换成大写格式 ， <code>false</code> 传换成小写格式
     * @return 十六进制String
     */
    public static String byteToHexStr(byte[] data, boolean toUpperCase) {
        return byteToHexStr(data, toUpperCase ? DIGITS_UPPER : DIGITS_LOWER);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制String
     */
    protected static String byteToHexStr(byte[] data, char[] toDigits) {
        return new String(byteToHex(data, toDigits));
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param data 十六进制字符串
     * @return byte[]
     * @throws RuntimeException 如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
     */
    public static byte[] hexToByte(String data) {
        char[] chars = data.toCharArray();
        int len = chars.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.  
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(chars[j], j) << 4;
            j++;
            f = f | toDigit(chars[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将十六进制字符转换成一个整数
     *
     * @param ch    十六进制char
     * @param index 十六进制字符在字符数组中的位置
     * @return 一个整数
     * @throws RuntimeException 当ch不是一个合法的十六进制字符时，抛出运行时异常
     */
    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch
                    + " at index " + index);
        }
        return digit;
    }

    /**
     * 十六进制字符串转十进制Long
     *
     * @param hex 十六进制字符串
     * @return 十进制Long
     */
    public static Long hexToDecLong(String hex) {
        return Long.decode(HEX_PREFIX + hex);
    }

    /**
     * 十六进制字符串转十进制Integer
     *
     * @param hex 十六进制字符串
     * @return 十进制Integer
     */
    public static Integer hexToDecInteger(String hex) {
        return Integer.decode(HEX_PREFIX + hex);
    }

    /**
     * 十进制数字转十六进制字符串
     *
     * @param num   十进制数字
     * @param digit 位数
     * @return 十六进制字符串
     */
    public static String numberToHex(String num, Integer digit) {
        String format = String.format("%%0%sx", digit);
        return String.format(format, Integer.parseInt(num));
    }

    /**
     * 十进制数转为二进制数
     *
     * @param num  十进制数
     * @param size 返回的位数
     * @return 二进制数
     */
    public static String decToBin(int num, int size) {
        if (size < (Integer.SIZE - Integer.numberOfLeadingZeros(num))) {
            throw new RuntimeException("传入size小于" + num + "二进制位数");
        }
        StringBuilder binStr = new StringBuilder();
        for (int i = size - 1; i >= 0; i--) {
            binStr.append(num >>> i & 1);
        }
        return binStr.toString();
    }

    /**
     * 将二进制转换成十六进制
     *
     * @param bin   二进制
     * @param digit 位数
     * @return 十六进制
     */
    public static String binToHex(String bin, Integer digit) {
        int dec = Integer.parseInt(bin, 2);
        return numberToHex(String.valueOf(dec), digit);
    }

    /**
     * 将十六进制转化成二进制
     *
     * @param hex      十六进制
     * @param byteSize 字节长度
     * @return 二进制
     */
    public static String hexToBin(String hex, Integer byteSize) {
        int dec = Integer.parseInt(hex, 16);
        return binLeftPad(Integer.toBinaryString(dec), "0", byteSize);
    }

    /**
     * 十六进制字符串转十六进制字节数组
     *
     * @param hexStr 十六进制字符串
     * @return 十六进制字节数组
     */
    public static String[] stringToHexArray(String hexStr) {
        if (hexStr.length() % 2 != 0) {
            throw new RuntimeException("十六进制字节串格式错误, 长度: " + hexStr.length());
        }
        String[] hexStrArr = new String[hexStr.length() / 2];
        char[] charArray = hexStr.toCharArray();
        for (int i = 0, j = 0; i < charArray.length; i += 2, j++) {
            hexStrArr[j] = charArray[i] + String.valueOf(charArray[i + 1]);
        }
        return hexStrArr;
    }

    /**
     * 二进制左填充
     *
     * @param binStr   二进制字符串
     * @param padding  二进制填充值
     * @param byteSize 期望字节长度
     * @return 填充结果
     */
    public static String binLeftPad(String binStr, String padding, int byteSize) {
        return strPadding(binStr, padding, true, byteSize * 8);
    }

    /**
     * 二进制右填充
     *
     * @param binStr   二进制字符串
     * @param padding  二进制填充值
     * @param byteSize 期望字节长度
     * @return 填充结果
     */
    public static String binRightPad(String binStr, String padding, int byteSize) {
        return strPadding(binStr, padding, false, byteSize * 8);
    }

    /**
     * 十六进制右填充
     *
     * @param hexStr   十六进制字符串
     * @param padding  十六进制填充值
     * @param byteSize 期望字节长度
     * @return 填充结果
     */
    public static String hexRightPad(String hexStr, String padding, int byteSize) {
        return strPadding(hexStr, padding, false, byteSize * 2);
    }

    /**
     * 字符串右填充
     *
     * @param str          字符串
     * @param padding      填充值
     * @param position     填充位置：true：左填充；false：右填充
     * @param expectLength 期望字符串长度
     * @return 填充结果
     */
    private static String strPadding(String str, String padding, boolean position, int expectLength) {
        if (str.length() > expectLength) {
            return str.substring(0, expectLength);
        }

        StringBuilder fill = new StringBuilder();
        for (int i = 0; i < expectLength - str.length(); i += padding.length()) {
            fill.append(padding);
        }

        str = position ? fill.toString() + str : str + fill.toString();
        if (str.length() > expectLength) {
            return str.substring(0, expectLength);
        }
        return str;
    }
}  