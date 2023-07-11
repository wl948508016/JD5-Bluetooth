package com.bdtd.ccg.common;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author hang.lv
 * @version 1.0.0
 * @since 2022/11/28 11:48
 */
public class ProtocolUtil {

    private static final Charset GB2312 = Charset.forName("GB2312");

    private static final String FORMAT_02D = "%02d";

    private static final String FORMAT_012X = "%012x";

    private static final String FORMAT_X = "%x";

    private static final String FORMAT_02X = "%02x";

    private static final String FORMAT_04X = "%04x";

    private static final BigDecimal MULTIPLE_1 = new BigDecimal("1");

    private static final BigDecimal MULTIPLE_10 = new BigDecimal("10");

    private static final BigDecimal MULTIPLE_100 = new BigDecimal("100");

    private static final String REGEX_SPOT = "\\.";

    private static final String EMPTY = "";

    /**
     * 构建任务数据协议
     * 帧头：传入则拼接到首部，不传则不拼接
     * 甲烷值：放大100倍，取整数部分
     * 氧气值：放大10倍，取整数部分
     * 一氧化碳：不放大，取整数部分
     * 二氧化碳：放大100倍，取整数部分
     * 温度值：放大10倍，取整数部分
     * <p>
     * 文本内容超出部分截断
     *
     * @param protocolData 数据
     * @return 十六进制字符串
     */
    public static String buildTaskDataProtocol(BizTaskDataProtocol protocolData) {
        String taskPeopleId = String.format(FORMAT_012X, protocolData.getTaskPeopleId());
        String classId = String.format(FORMAT_02X, protocolData.getClassId());
        String checkTimes = String.format(FORMAT_02X, protocolData.getCheckTimes());
        String checkTime = transformDateTimeByDec(protocolData.getCheckTime());
        String workUserName = HexUtil.hexRightPad(HexUtil.byteToHexStr(protocolData.getWorkUserName().getBytes(GB2312)), "00", 8);
        String methaneVal = String.format(FORMAT_04X, enlargeData(protocolData.getMethaneVal(), MULTIPLE_100));
        String oxygenVal = String.format(FORMAT_04X, enlargeData(protocolData.getOxygenVal(), MULTIPLE_10));
        String carbonMonoxideVal = String.format(FORMAT_04X, Integer.parseInt(protocolData.getCarbonMonoxideVal()));
        String carbonDioxideVal = String.format(FORMAT_04X, enlargeData(protocolData.getCarbonDioxideVal(), MULTIPLE_100));
        String temperatureVal = String.format(FORMAT_04X, enlargeData(protocolData.getTemperatureVal(), MULTIPLE_10));

        // 保留值：6个字节
        String retentionVal = "000000000000";
        String data = protocolData.getFrameHeader() + taskPeopleId + classId + checkTimes + checkTime + workUserName + methaneVal
                + oxygenVal + carbonMonoxideVal + carbonDioxideVal + temperatureVal + retentionVal;

        return data.toUpperCase();
    }

    /**
     * 构建白板上传任务数据到平台的响应数据
     *
     * @param result true：处理成功，false：处理失败
     * @return 应答数据
     */
    public static String buildTaskDataResponse(boolean result) {
        return "0143" + (result ? "00" : "01");
    }

    /**
     * 解析任务数据协议响应
     *
     * @param responseValue 响应数据，十六进制字符串
     * @return 响应结果
     */
    public static ProtocolResponse parseTaskDataResponse(String responseValue) {
        String[] hexByteValue = HexUtil.stringToHexArray(responseValue);
        if ("00".equals(hexByteValue[2])) {
            return ProtocolResponse.success();
        }
        if ("01".equals(hexByteValue[2])) {
            return ProtocolResponse.error("0x" + hexByteValue[2], "接收成功，上传服务器失败");
        }
        if ("05".equals(hexByteValue[2])) {
            return ProtocolResponse.error("0x" + hexByteValue[2], "接收失败，上传服务器失败");
        }
        return ProtocolResponse.error("0x" + hexByteValue[2], "响应异常");
    }

    /**
     * 解析协议数据
     *
     * @param responseValue 十六进制字符串
     * @return 响应结果
     */
    public static BizTaskDataProtocol parseTaskDataProtocol(String responseValue) {
        BizTaskDataProtocol data = new BizTaskDataProtocol();
        char[] dataArray = responseValue.substring(4).toCharArray();
        data.setTaskPeopleId(HexUtil.hexToDecLong(subCharArray(dataArray, 0, 12)));
        data.setClassId(HexUtil.hexToDecLong(subCharArray(dataArray, 12, 2)));
        data.setCheckTimes(HexUtil.hexToDecInteger(subCharArray(dataArray, 14, 2)));
        data.setCheckTime(DateUtils.getSouStrToDateStr("20" + subCharArray(dataArray, 16, 12)));
        data.setWorkUserName(new String(HexUtil.hexToByte(subCharArray(dataArray, 28, 16)), GB2312).trim());
        data.setMethaneVal(reduceData(HexUtil.hexToDecInteger(subCharArray(dataArray, 44, 4)), MULTIPLE_100, 2));
        data.setOxygenVal(reduceData(HexUtil.hexToDecInteger(subCharArray(dataArray, 48, 4)), MULTIPLE_10, 1));
        data.setCarbonMonoxideVal(reduceData(HexUtil.hexToDecInteger(subCharArray(dataArray, 52, 4)), MULTIPLE_1, 0));
        data.setCarbonDioxideVal(reduceData(HexUtil.hexToDecInteger(subCharArray(dataArray, 56, 4)), MULTIPLE_100, 2));
        data.setTemperatureVal(reduceData(HexUtil.hexToDecInteger(subCharArray(dataArray, 60, 4)), MULTIPLE_10, 1));

        return data;
    }

    /**
     * 构建Modbus-Rtu批量写入保持寄存器协议<br>
     * 单个协议数据：multiValue[0]，取索引下标为0的数据<br>
     * 1. 设置当前时间，格式为yyyy-MM-dd HH:mm:ss<br>
     * 2. IP地址、子网掩码、网关、DNS：字符串格式，如：192.168.0.101<br>
     * 3. 端口号：十进制字符串，如7085<br>
     * 4. WIFI的SSID、密码<br>
     * 5. 白板位置、标题、主题<br>
     * <p>
     * 多个协议数据：<br>
     * 1. 主机IP地址和端口号：[0]：服务器IP地址，[1]：端口号<br>
     * 2. 连接WIFI的SSID及密码的字节数和值，及网络设置(IP地址、子网掩码、网关、DNS)：[0]: SSID编码格式，[1]: 密码编码格式，[2]: WIFI的SSID，
     * [3]: WIFI的密码，[4]：WIFI的本机IP地址，[5]：WIFI的子网掩码，[6]：WIFI的网关，[7]：WIFI DNS<br>
     * 3. 连接WiFi的SSID及密码的字节数：[0]: SSID编码格式，[1]: SSID字节数，[2]: 密码编码格式，[3]: 密码字节数（0为GB2312格式，1为UTF-8格式）<br>
     * 4. 本机网络设置(IP地址、子网掩码、网关、DNS)：[0]：本机IP地址，[1]：本机子网掩码，[2]：本机网关，[3]：本机DNS<br>
     * 5. 班次配置：[0]: 每天班数，[1]: 每班巡检次数，[2]: 第一班开始时间HH:mm<br>
     * 6. 预警使能配置：每一位的值表示1个巡检参数的预警使能，(0：不使能，1: 使能)，数组索引[0, 4]表示上限预警，索引[5, 9]表示下限预警如：["1", "0", "1", "1", "0", "1", "1", "0", "1", "1"]<br>
     * 7. 预警上限值、预警下限值：十进制各个巡检参数的预警值，如：["53", "20", "100", "23", "320"]<br>
     * 8. 预警颜色值：数值0~5，分别代表红、绿、蓝、黄、紫、青，如：["0", "1", "3", "1", "5"]<br>
     * 注：6、7、8每个参数配置的参数顺序：甲烷、氧气、一氧化碳、二氧化碳、温度
     *
     * @param protocolType 寄存器
     * @param multiData    多个协议数据
     * @return 十六进制字符串
     */
    public static String buildModbusRtuWriteProtocol(ProtocolTypeEnum protocolType, String[] multiData) {
        String registerAddress = String.format(FORMAT_04X, Math.abs(protocolType.getRegisterAddress()));
        String registerNumber = String.format(FORMAT_04X, protocolType.getRegisterNumber());
        String byteSize = String.format(FORMAT_02X, protocolType.getRegisterNumber() * 2);
        String registerValue = buildRegisterValue(protocolType, multiData);

        if (registerValue.length() != protocolType.getRegisterNumber() * 2 * 2) {
            throw new RuntimeException("协议数据构建异常, 寄存器值长度不一致");
        }

        String protocolData = "0110" + registerAddress + registerNumber + byteSize + registerValue;
        return protocolData.toUpperCase();
    }

    /**
     * 解析Modbus-Rtu协议响应
     *
     * @param functionCode  功能码，单个读取：03；批量写入：10
     * @param responseValue 响应数据，十六进制字符串
     * @return 响应结果
     */
    public static ProtocolResponse parseModbusRtuResponse(String functionCode, String responseValue) {
        String[] hexByteValue = HexUtil.stringToHexArray(responseValue);
        if (!functionCode.equals(hexByteValue[1])) {
            return ProtocolResponse.error(hexByteValue[1], hexByteValue[2]);
        }
        ProtocolTypeEnum protocolTypeEnum = ProtocolTypeEnum.fromRegisterAddress(Integer.parseInt(hexByteValue[2] + hexByteValue[3], 16));
        return ProtocolResponse.success(protocolTypeEnum);
    }

    /**
     * 构建Modbus-Rtu读寄存器协议
     *
     * @param protocolType 寄存器
     * @return 十六进制字符串
     */
    public static String buildModbusRtuReadProtocol(ProtocolTypeEnum protocolType) {
        String registerAddress = String.format(FORMAT_04X, protocolType.getRegisterAddress() - 1);
        String registerNumber = String.format(FORMAT_04X, protocolType.getRegisterNumber());

        String protocolData = "0103" + registerAddress + registerNumber;
        return protocolData.toUpperCase();
    }

    /**
     * 解析Modbus-Rtu读寄存器协议响应
     * 单个响应数据：multiValue[0]，取索引下标为0的数据<br>
     * 1. IP地址、子网掩码、网关、DNS：字符串格式，如：192.168.0.101<br>
     * 2. 端口号：十进制字符串，如：7085<br>
     * 3. 电子白板唯一编码：字符串，如：1AFE3697D57B<br>
     * <p>
     * 多个响应数据：<br>
     * 1. 主机IP地址和端口号：[0]：IP地址，[1]：端口号<br>
     * 2. 连接WIFI的SSID及密码的字节数和值，及网络设置(IP地址、子网掩码、网关、DNS)：[0]: SSID编码格式，[1]: SSID字节数，[2]: 密码编码格式，
     * [3]: 密码字节数（0为GB2312格式，1为UTF-8格式），[4]: WIFI的SSID，[5]: WIFI的密码，[6]：WIFI的本机IP地址，[7]：WIFI的子网掩码，
     * [8]：WIFI的网关，[9]：WIFI DNS<br>
     * 3. 本机网络设置(IP地址、子网掩码、网关、DNS)：[0]：本机IP地址，[1]：本机子网掩码，[2]：本机网关，[3]：本机DNS<br>
     * 4. 班次配置：[0]: 每天班数, [1]: 每班巡检次数, [2]: 第一班开始时间HH:mm<br>
     * 5. 预警使能配置：每一位的值表示1个巡检参数的预警使能，(0：不使能，1: 使能)，数组索引[0, 4]表示上限预警，索引[5, 9]表示下限预警如：["1", "0", "1", "1", "0", "1", "1", "0", "1", "1"]<br>
     * 6. 预警上限值、预警下限值：十进制各个巡检参数的预警值，如：["53", "20", "100", "23", "320"]<br>
     * 7. 预警颜色值：数值0~5，分别代表红、绿、蓝、黄、紫、青，如：["0", "1", "3", "1", "5"]<br>
     * 注：5、6、7每个参数配置的参数顺序：甲烷、氧气、一氧化碳、二氧化碳、温度<br>
     *
     * @param protocolType  寄存器
     * @param responseValue 响应数据，十六进制字符串
     * @return 响应结果
     */
    public static String[] parseModbusRtuReadResponse(ProtocolTypeEnum protocolType, String responseValue) {
        String[] hexByteValue = HexUtil.stringToHexArray(responseValue);
        int dataLength = Integer.parseInt(hexByteValue[2], 16);
        String[] data = new String[dataLength];
        System.arraycopy(hexByteValue, 3, data, 0, dataLength);

        return parseRegisterValue(protocolType, data);
    }

    /**
     * 校验CRC-16
     *
     * @param responseValue 十六进制数据
     * @return true：符合，false：不符合
     */
    public static boolean checkCrc16(String responseValue) {
        String originProtocol = responseValue.substring(0, responseValue.length() - 4);
        String verifyProtocol = Crc16Util.getData(originProtocol);
        return responseValue.equalsIgnoreCase(verifyProtocol);
    }

    /**
     * 解析寄存器值
     *
     * @param protocolType 寄存器
     * @param responseData 响应数据，字节数组
     * @return 响应结果
     */
    public static String[] parseRegisterValue(ProtocolTypeEnum protocolType, String[] responseData) {
        String[] data = new String[1];
        switch (protocolType) {
            case WHITEBOARD_CODE:
                data[0] = String.join("", responseData);
                break;
            case SERVER_IP_ADDRESS_PORT:
                return analysisServerIpPortValue(responseData);
            case SERVER_IP_ADDRESS:
            case WIFI_IP_ADDRESS:
            case WIFI_SUBNET_MASK:
            case WIFI_GATEWAY:
            case WIFI_DNS:
            case LOCAL_IP_ADDRESS:
            case LOCAL_SUBNET_MASK:
            case LOCAL_GATEWAY:
            case LOCAL_DNS:
                data[0] = analysisIpValue(responseData);
                break;
            case SERVER_PORT:
            case RS485_ADDRESS:
            case WHITEBOARD_THEME:
                data[0] = analysisIntValue(responseData);
                break;
            case WIFI_SSID_PASSWORD_NETWORK:
                return analysisWifiSsidPasswordNetworkValue(responseData);
            case WIFI_SSID_PASSWORD_BYTE_SIZE:
            case WIFI_SSID:
            case WIFI_PASSWORD:
                return data;
            case LOCAL_NETWORK:
                return analysisNetworkSetting(responseData);
            case WHITEBOARD_LOCATION:
            case WHITEBOARD_TITLE:
                data[0] = analysisTextValue(responseData).trim();
                return data;
            case CLASS_CONFIG:
                return analysisClassValue(responseData);
            case ALERT_ENABLE_CONFIGURATION:
                return analysisAlertEnableValue(responseData);
            case ALERT_UPPER_LIMIT:
            case ALERT_LOWER_LIMIT:
                return analysisAlertValue(responseData);
            case ALERT_COLOR_CONFIG:
                return analysisAlertColorValue(responseData);
        }
        return data;
    }

    /**
     * 构建寄存器值
     *
     * @param protocolType 寄存器
     * @param multiValue   多个协议数据
     * @return 十六进制寄存器值
     */
    public static String buildRegisterValue(ProtocolTypeEnum protocolType, String[] multiValue) {
        String value = multiValue[0];
        switch (protocolType) {
            case SET_CURRENT_TIME:
                return transformDateTimeByBit(value);
            case SERVER_IP_ADDRESS_PORT:
                return generateServerIpPortValue(multiValue);
            case SERVER_IP_ADDRESS:
            case WIFI_IP_ADDRESS:
            case WIFI_SUBNET_MASK:
            case WIFI_GATEWAY:
            case WIFI_DNS:
            case LOCAL_IP_ADDRESS:
            case LOCAL_SUBNET_MASK:
            case LOCAL_GATEWAY:
            case LOCAL_DNS:
                return generateIpValue(value);
            case SERVER_PORT:
            case WHITEBOARD_THEME:
                return HexUtil.numberToHex(value, 4);
            case WIFI_SSID_PASSWORD_NETWORK:
                return generateWifiSsidPasswordNetworkValue(multiValue);
            case WIFI_SSID_PASSWORD_BYTE_SIZE:
                return generateWifiSsidPasswordByteSizeValue(multiValue, ProtocolTypeEnum.WIFI_SSID_PASSWORD_BYTE_SIZE.getRegisterNumber() * 2);
            case WIFI_SSID:
                return generateTextValue(value, ProtocolTypeEnum.WIFI_SSID.getRegisterNumber() * 2);
            case WIFI_PASSWORD:
                return generateTextValue(value, ProtocolTypeEnum.WIFI_PASSWORD.getRegisterNumber() * 2);
            case LOCAL_NETWORK:
                return generateLocalNetworkValue(multiValue);
            case WHITEBOARD_LOCATION:
                return generateTextValue(value, ProtocolTypeEnum.WHITEBOARD_LOCATION.getRegisterNumber() * 2);
            case WHITEBOARD_TITLE:
                return generateTextValue(value, ProtocolTypeEnum.WHITEBOARD_TITLE.getRegisterNumber() * 2);
            case RS485_ADDRESS:
                return generateRS485Value(value);
            case CLASS_CONFIG:
                return generateClassValue(multiValue);
            case ALERT_ENABLE_CONFIGURATION:
                return generateAlertEnableValue(multiValue, ProtocolTypeEnum.ALERT_ENABLE_CONFIGURATION.getRegisterNumber() * 2);
            case ALERT_UPPER_LIMIT:
                return generateAlertValue(multiValue, ProtocolTypeEnum.ALERT_UPPER_LIMIT.getRegisterNumber() * 2);
            case ALERT_LOWER_LIMIT:
                return generateAlertValue(multiValue, ProtocolTypeEnum.ALERT_LOWER_LIMIT.getRegisterNumber() * 2);
            case ALERT_COLOR_CONFIG:
                return generateAlertColorValue(multiValue, ProtocolTypeEnum.ALERT_COLOR_CONFIG.getRegisterNumber() * 2);
        }
        return EMPTY;
    }

    /**
     * 构建本机IP地址和端口号
     * [0]：服务器IP地址，[1]：端口号
     *
     * @param multiValue 协议数据
     * @return 十六进制数据
     */
    private static String generateServerIpPortValue(String[] multiValue) {
        return generateIpValue(multiValue[0]) + HexUtil.numberToHex(multiValue[1], 4);
    }

    /**
     * 解析本机IP地址和端口号
     *
     * @param responseData 响应数据
     * @return [0]：IP地址，[1]：端口号
     */
    private static String[] analysisServerIpPortValue(String[] responseData) {
        String[] data = new String[2];

        // 解析服务器IP地址
        String[] serverIp = new String[ProtocolTypeEnum.SERVER_IP_ADDRESS.getRegisterNumber() * 2];
        System.arraycopy(responseData, 0, serverIp, 0, serverIp.length);
        data[0] = analysisIpValue(serverIp);

        // 解析本机端口号
        String[] port = new String[ProtocolTypeEnum.SERVER_PORT.getRegisterNumber() * 2];
        System.arraycopy(responseData, serverIp.length, port, 0, port.length);
        data[1] = analysisIntValue(port);
        return data;
    }

    /**
     * 构建WIFI的SSID及密码的字节数和值，及网络设置(IP地址、子网掩码、网关、DNS)
     * 连接WIFI的SSID及密码的字节数：
     * [0]: SSID编码格式, [1]: 密码编码格式（0为GB2312格式，1为UTF-8格式）
     * <p>
     * 连接WIFI的SSID和密码：
     * [2]: WIFI的SSID，[3]: WIFI的密码
     * <p>
     * 连接WIFI的本机IP地址、子网掩码、网关、DNS：
     * [4]：WIFI的本机IP地址，[5]：WIFI的子网掩码，[6]：WIFI的网关，[7]：WIFI DNS
     *
     * @param multiValue 协议数据
     * @return 十六进制数据
     */
    private static String generateWifiSsidPasswordNetworkValue(String[] multiValue) {
        StringBuilder builder = new StringBuilder();
        builder.append(generateWifiSsidPasswordValue(multiValue));
        for (int i = 4; i < multiValue.length; i++) {
            builder.append(generateIpValue(multiValue[i]));
        }
        return builder.toString();
    }

    /**
     * 解析WIFI的SSID及密码的字节数、编码格式、网络配置
     *
     * @param responseData 响应数据
     * @return [0]: SSID编码格式, [1]: SSID字节数，[2]: 密码编码格式，[3]: 密码字节数（0为GB2312格式，1为UTF-8格式），[4]: WIFI的SSID，
     * [5]: WIFI的密码，[6]：WIFI的本机IP地址，[7]：WIFI的子网掩码，[8]：WIFI的网关，[9]：WIFI DNS
     */
    private static String[] analysisWifiSsidPasswordNetworkValue(String[] responseData) {
        String[] data = new String[10];
        // 解析WIFI的SSID及密码的字节数和值
        String[] ssidPasswordData = analysisWifiSsidPasswordValue(responseData);
        System.arraycopy(ssidPasswordData, 0, data, 0, ssidPasswordData.length);

        // 解析WIFI本机IP、子网掩码、网关、DNS
        String[] networkSetting = new String[16];
        int srcPos = (ProtocolTypeEnum.WIFI_SSID_PASSWORD_BYTE_SIZE.getRegisterNumber() + ProtocolTypeEnum.WIFI_SSID.getRegisterNumber()
                + ProtocolTypeEnum.WIFI_PASSWORD.getRegisterNumber()) * 2;
        System.arraycopy(responseData, srcPos, networkSetting, 0, networkSetting.length);
        String[] networkSettingData = analysisNetworkSetting(networkSetting);
        System.arraycopy(networkSettingData, 0, data, ssidPasswordData.length, networkSettingData.length);

        return data;
    }

    /**
     * 构建WIFI的SSID及密码的字节数和值
     * 连接WIFI的SSID及密码的字节数：
     * [0]: SSID编码格式, [1]: 密码编码格式（0为GB2312格式，1为UTF-8格式）
     * <p>
     * 连接WIFI的SSID和密码：
     * [2]: WIFI的SSID，[3]: WIFI的密码
     *
     * @param multiValue 协议数据
     * @return 十六进制数据
     */
    private static String generateWifiSsidPasswordValue(String[] multiValue) {
        // 构建WIFI的SSID
        String validWifiSsid = generateTextValue(multiValue[2]);
        String wifiSsid = HexUtil.hexRightPad(validWifiSsid, "00", ProtocolTypeEnum.WIFI_SSID.getRegisterNumber() * 2);
        // 构建WIFI的密码
        String validWifiPassword = generateTextValue(multiValue[3]);
        String wifiPassword = HexUtil.hexRightPad(validWifiPassword, "00", ProtocolTypeEnum.WIFI_PASSWORD.getRegisterNumber() * 2);

        // 构建WIFI的SSID及密码的字节数寄存器值
        String[] byteSizeValue = new String[4];
        // SSID编码格式
        byteSizeValue[0] = multiValue[0];
        // SSID字节数
        byteSizeValue[1] = String.valueOf(validWifiSsid.length() / 2);
        // 密码编码格式
        byteSizeValue[2] = multiValue[1];
        // 密码字节数
        byteSizeValue[3] = String.valueOf(validWifiPassword.length() / 2);
        String wifiSsidPasswordByteSize = generateWifiSsidPasswordByteSizeValue(byteSizeValue, ProtocolTypeEnum.WIFI_SSID_PASSWORD_BYTE_SIZE.getRegisterNumber() * 2);
        return wifiSsidPasswordByteSize + wifiSsid + wifiPassword;
    }

    /**
     * 构建WIFI的SSID及密码的字节数寄存器值
     * [0]: SSID编码格式, [1]: SSID字节数, [2]: 密码编码格式, [3]: 密码字节数（0为GB2312格式，1为UTF-8格式）
     *
     * @param multiValue 协议数据
     * @param byteSize   期望字节长度
     * @return 十六进制数据
     */
    private static String generateWifiSsidPasswordByteSizeValue(String[] multiValue, Integer byteSize) {
        String ssidByteSize = HexUtil.decToBin(Integer.parseInt(multiValue[1]), 7);
        String passwordByteSize = HexUtil.decToBin(Integer.parseInt(multiValue[3]), 7);
        String binValue = multiValue[0] + ssidByteSize + multiValue[2] + passwordByteSize;
        return HexUtil.binToHex(binValue, byteSize * 2);
    }

    /**
     * 解析WIFI的SSID及密码的字节数和值
     * [0]: SSID编码格式, [1]: SSID字节数，[2]: 密码编码格式，[3]: 密码字节数（0为GB2312格式，1为UTF-8格式），[4]: WIFI的SSID，[5]: WIFI的密码
     *
     * @param responseData 响应数据
     * @return WIFI的SSID及密码的字节数和值
     */
    private static String[] analysisWifiSsidPasswordValue(String[] responseData) {
        String[] data = new String[6];
        String[] byteSize = new String[ProtocolTypeEnum.WIFI_SSID_PASSWORD_BYTE_SIZE.getRegisterNumber() * 2];

        // 解析WiFi的SSID及密码的字节数数据
        System.arraycopy(responseData, 0, byteSize, 0, byteSize.length);
        String byteSizeBin = HexUtil.hexToBin(String.join("", byteSize), byteSize.length);
        Charset ssidCharset = byteSizeBin.charAt(0) == 48 ? GB2312 : StandardCharsets.UTF_8;
        int ssidByteSize = Integer.parseInt(byteSizeBin.substring(1, 8), 2);
        Charset passwordCharset = byteSizeBin.charAt(8) == 48 ? GB2312 : StandardCharsets.UTF_8;
        int passwordByteSize = Integer.parseInt(byteSizeBin.substring(9), 2);
        data[0] = String.valueOf(byteSizeBin.charAt(0));
        data[1] = String.valueOf(ssidByteSize);
        data[2] = String.valueOf(byteSizeBin.charAt(8));
        data[3] = String.valueOf(passwordByteSize);

        // 解析WiFi的SSID
        String[] ssid = new String[ssidByteSize];
        String[] password = new String[passwordByteSize];

        System.arraycopy(responseData, byteSize.length, ssid, 0, ssidByteSize);
        data[4] = new String(HexUtil.hexToByte(String.join("", ssid)), ssidCharset);

        // 解析WiFi的密码
        int srcPos = (ProtocolTypeEnum.WIFI_SSID_PASSWORD_BYTE_SIZE.getRegisterNumber() + ProtocolTypeEnum.WIFI_SSID.getRegisterNumber()) * 2;
        System.arraycopy(responseData, srcPos, password, 0, passwordByteSize);
        data[5] = new String(HexUtil.hexToByte(String.join("", password)), passwordCharset);

        return data;
    }

    /**
     * 构建本机网络设置(IP地址、子网掩码、网关、DNS)
     * [0]：本机IP地址，[1]：本机子网掩码，[2]：本机网关，[3]：本机DNS
     *
     * @param multiValue 协议数据
     * @return 十六进制数据
     */
    private static String generateLocalNetworkValue(String[] multiValue) {
        StringBuilder builder = new StringBuilder();
        for (String value : multiValue) {
            builder.append(generateIpValue(value));
        }
        return builder.toString();
    }

    /**
     * 构建本机网络设置(IP地址、子网掩码、网关、DNS)
     *
     * @param responseData 响应数据
     * @return [0]：本机IP地址，[1]：本机子网掩码，[2]：本机网关，[3]：本机DNS
     */
    private static String[] analysisNetworkSetting(String[] responseData) {
        String[] data = new String[4];
        for (int i = 0, j = 0; i < responseData.length; i += 4, j++) {
            String[] temp = new String[4];
            System.arraycopy(responseData, i, temp, 0, temp.length);
            data[j] = analysisIpValue(temp);
        }
        return data;
    }

    /**
     * 构建IP类寄存器值
     *
     * @param value 协议数据
     * @return 十六进制数据
     */
    private static String generateIpValue(String value) {
        String[] split = value.split(REGEX_SPOT);
        StringBuilder builder = new StringBuilder();
        for (String item : split) {
            builder.append(String.format(FORMAT_02X, Integer.parseInt(item)));
        }

        return builder.toString();
    }

    /**
     * 解析IP类寄存器值
     *
     * @param responseData 响应数据
     * @return IP地址
     */
    private static String analysisIpValue(String[] responseData) {
        return Arrays.stream(responseData).map(item -> Integer.parseInt(item, 16)).map(String::valueOf)
                .collect(Collectors.joining("."));
    }

    /**
     * 解析整数类型寄存器值
     *
     * @param responseData 响应数据
     * @return 整数字符串
     */
    private static String analysisIntValue(String[] responseData) {
        String strData = String.join("", responseData);
        return String.valueOf(Integer.parseInt(strData, 16));
    }

    /**
     * 构建文本寄存器值
     *
     * @param value    协议数据
     * @param byteSize 期望字节长度
     * @return 十六进制数据
     */
    private static String generateTextValue(String value, Integer byteSize) {
        return HexUtil.hexRightPad(generateTextValue(value), "00", byteSize);
    }

    /**
     * 构建文本寄存器值
     *
     * @param value 协议数据
     * @return 十六进制数据
     */
    private static String generateTextValue(String value) {
        return HexUtil.byteToHexStr(value.getBytes(GB2312));
    }

    /**
     * 解析文本寄存器值
     *
     * @param responseData 响应数据
     * @return 文本值
     */
    private static String analysisTextValue(String[] responseData) {
        return new String(HexUtil.hexToByte(String.join("", responseData)), GB2312);
    }

    /**
     * 构建修改RS485地址寄存器值
     * RS485地址：[0]：设备地址
     *
     * @param value 协议数据
     * @return 十六进制数据
     */
    private static String generateRS485Value(String value) {
        String registerValue = String.format(FORMAT_02X, Integer.parseInt(value));

        return "00" + registerValue;
    }

    /**
     * 构建配置班次协议数据
     * 班次配置：[0]: 每天班数, [1]: 每班巡检次数, [2]: 第一班开始时间HH:mm
     *
     * @param multiValue 协议数据
     * @return 十六进制数据
     */
    private static String generateClassValue(String[] multiValue) {
        String[] split = multiValue[2].split(":");
        String hourHex = String.format(FORMAT_02X, Integer.parseInt(split[0]));
        String classNumberHex = String.format(FORMAT_X, Integer.parseInt(multiValue[0]));
        String checkTimesHex = String.format(FORMAT_X, Integer.parseInt(multiValue[1]));

        return hourHex + classNumberHex + checkTimesHex;
    }

    /**
     * 解析配置班次寄存器值
     *
     * @param responseData 响应数据
     * @return 班次配置：[0]: 每天班数, [1]: 每班巡检次数, [2]: 第一班开始时间HH:mm
     */
    private static String[] analysisClassValue(String[] responseData) {
        // 每天半数：低字节低4位
        String classNumber = String.valueOf(Integer.parseInt(String.valueOf(responseData[1].charAt(1)), 16));
        // 每班巡检次数：低字节高4位
        String checkTimes = String.valueOf(Integer.parseInt(String.valueOf(responseData[1].charAt(0)), 16));
        // 第一班开始时间：高字节（HH），默认mm为00
        int hour = Integer.parseInt(responseData[0], 16);
        String startTime = (hour < 10 ? "0" + hour : hour) + ":00";

        return new String[]{classNumber, checkTimes, startTime};
    }

    /**
     * 构建预警使能寄存器值
     *
     * @param value    协议数据，[高字节上限值, 低字节下限值]
     * @param byteSize 期望字节长度
     * @return 十六进制数据
     */
    private static String generateAlertEnableValue(String[] value, Integer byteSize) {
        String[] arr = new String[byteSize * 8];
        Arrays.fill(arr, "0");
        // 数组翻转，寄存器每位参数配置顺序自右向左，[低字节下限值, 高字节上限值]
        arrayReverse(value);
        // 复制高字节预警上限值
        System.arraycopy(value, 5, arr, 3, 5);
        // 复制低字节预警下限值
        System.arraycopy(value, 0, arr, 11, 5);
        return HexUtil.binToHex(strArrToString(arr), byteSize * 2);
    }

    /**
     * 解析预警使能寄存器值
     *
     * @param responseData 响应数据，2个字节，0xFFFF
     * @return 每一位的值表示1个巡检参数的预警使能，(0：不使能，1: 使能)，数组索引[0, 4]表示上限预警，索引[5, 9]表示下限预警如：["1", "0", "1", "1", "0", "1", "1", "0", "1", "1"]
     */
    private static String[] analysisAlertEnableValue(String[] responseData) {
        // [低字节下限值, 高字节上限值]
        String[] data = new String[10];
        String alertEnableBin = HexUtil.hexToBin(String.join("", responseData), ProtocolTypeEnum.ALERT_ENABLE_CONFIGURATION.getRegisterNumber() * 2);
        String[] alertEnableBinArr = alertEnableBin.split("");

        // 复制高字节预警上限值
        System.arraycopy(alertEnableBinArr, 3, data, 5, 5);
        // 复制低字节预警下限值
        System.arraycopy(alertEnableBinArr, 11, data, 0, 5);

        return arrayReverse(data);
    }

    /**
     * 构建预警阈值寄存器值
     *
     * @param value    协议数据
     * @param byteSize 期望字节长度
     * @return 十六进制数据
     */
    private static String generateAlertValue(String[] value, Integer byteSize) {
        StringBuilder builder = new StringBuilder();
        for (String item : value) {
            builder.append(String.format(FORMAT_04X, Integer.parseInt(item)));
        }

        return HexUtil.hexRightPad(builder.toString(), "00", byteSize);
    }

    /**
     * 解析预警阈值寄存器值
     *
     * @param responseData 响应数据，16个字节，每个参数占两个字节
     * @return 预警阈值寄存器值
     */
    private static String[] analysisAlertValue(String[] responseData) {
        String[] data = new String[5];
        for (int i = 0, j = 0; i < data.length; i++, j += 2) {
            data[i] = String.valueOf(Integer.parseInt(responseData[j] + responseData[j + 1], 16));
        }

        return data;
    }

    /**
     * 构建预警颜色寄存器值
     * 0x4321，0x8765 对应1－8参数顺序
     *
     * @param value    协议数据
     * @param byteSize 期望字节长度
     * @return 十六进制数据
     */
    private static String generateAlertColorValue(String[] value, Integer byteSize) {
//        StringBuilder builder = new StringBuilder();
//        for (String item : value) {
//            builder.append(String.format(FORMAT_X, Integer.parseInt(item)));
//        }
//
//        return HexUtil.hexRightPad(builder.toString(), "00", byteSize);
        return String.format(FORMAT_X, Integer.parseInt(value[3])) +
                String.format(FORMAT_X, Integer.parseInt(value[2])) +
                String.format(FORMAT_X, Integer.parseInt(value[1])) +
                String.format(FORMAT_X, Integer.parseInt(value[0])) +
                "000" +
                String.format(FORMAT_X, Integer.parseInt(value[4]));
    }

    /**
     * 解析预警颜色寄存器值
     *
     * @param responseData 响应数据，4个字节，每个参数占4位，0xFFFF 0xFFFF
     * @return 预警颜色寄存器值
     */
    private static String[] analysisAlertColorValue(String[] responseData) {
        String[] data = new String[5];
        String[] join = String.join("", responseData).split("");
        for (int i = 0; i < data.length; i++) {
            data[i] = String.valueOf(Integer.parseInt(join[i], 16));
        }

        return data;
    }

    /**
     * 巡检时间转化为十六进制字符串
     *
     * @param date 巡检时间，yyyy-MM-dd HH:mm:ss
     * @return 6字节长度的十六进制字符串
     */
    public static String transformDateTimeByDec(String date) {
        LocalDateTime checkTime = DateUtils.transformLocalDateTime(date);
        return String.format(FORMAT_02D, checkTime.getYear()).substring(2, 4) +
                String.format(FORMAT_02D, checkTime.getMonthValue()) +
                String.format(FORMAT_02D, checkTime.getDayOfMonth()) +
                String.format(FORMAT_02D, checkTime.getHour()) +
                String.format(FORMAT_02D, checkTime.getMinute()) +
                String.format(FORMAT_02D, checkTime.getSecond());
    }

    /**
     * 巡检时间转化为十六进制字符串
     *
     * @param date 巡检时间，yyyy-MM-dd HH:mm:ss
     * @return 4字节长度的十六进制字符串
     */
    public static String transformDateTimeByBit(String date) {
        LocalDateTime checkTime = DateUtils.transformLocalDateTime(date);
        String year = String.valueOf(checkTime.getYear()).substring(2);
        String binString = HexUtil.decToBin(Integer.parseInt(year), 6) +
                HexUtil.decToBin(checkTime.getMonthValue(), 4) +
                HexUtil.decToBin(checkTime.getDayOfMonth(), 5) +
                HexUtil.decToBin(checkTime.getHour(), 5) +
                HexUtil.decToBin(checkTime.getMinute(), 6) +
                HexUtil.decToBin(checkTime.getSecond(), 6);
        return HexUtil.binToHex(binString, 4).toUpperCase();
    }

    /**
     * 截取字符数组
     *
     * @param srcData    原字符数组
     * @param startIndex 开始索引
     * @param length     长度
     * @return 目标字符串
     */
    private static String subCharArray(char[] srcData, int startIndex, int length) {
        char[] dest = new char[length];
        System.arraycopy(srcData, startIndex, dest, 0, length);
        return String.valueOf(dest);
    }

    /**
     * 数据放大
     *
     * @param val      原始数据
     * @param multiple 放大倍数
     * @return 放大结果
     */
    private static Integer enlargeData(String val, BigDecimal multiple) {
        BigDecimal temp = new BigDecimal(val);
        return temp.multiply(multiple).toBigInteger().intValue();
    }

    /**
     * 数据缩小
     *
     * @param val      放大数据
     * @param multiple 缩小倍数
     * @param scale    小数点位数
     * @return 原始数据
     */
    private static String reduceData(Integer val, BigDecimal multiple, int scale) {
        BigDecimal temp = new BigDecimal(val);
        return temp.divide(multiple, scale, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    /**
     * 字符串数组转字符串
     *
     * @param array 字符串数组
     * @return 字符串
     */
    private static String strArrToString(String[] array) {
        StringBuilder builder = new StringBuilder();
        for (String item : array) {
            builder.append(item);
        }
        return builder.toString();
    }

    /**
     * 数组翻转
     *
     * @param array 源数组
     * @return 翻转后的数组
     */
    private static String[] arrayReverse(String[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            String temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
        return array;
    }
}
