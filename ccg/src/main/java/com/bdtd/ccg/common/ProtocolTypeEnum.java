package com.bdtd.ccg.common;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hang.lv
 * @version 1.0.0
 * @since 2022/12/7 14:11
 */
public enum ProtocolTypeEnum {

    WHITEBOARD_CODE(40005, 3, "电子白板唯一编码"),
    SERVER_IP_ADDRESS_PORT(40065, 3, "主机IP地址和端口号"),
    SERVER_IP_ADDRESS(-40065, 2, "主机IP地址"),
    SERVER_PORT(40067, 1, "主机端口号"),
    WIFI_SSID_PASSWORD_NETWORK(40068, 57, "连接WIFI的SSID及密码的字节数和值，及网络设置(IP地址、子网掩码、网关、DNS)"),
    WIFI_SSID_PASSWORD_BYTE_SIZE(-40068, 1, "连接WIFI的SSID及密码的字节数"),
    WIFI_SSID(40069, 16, "连接WIFI的SSID"),
    WIFI_PASSWORD(40085, 32, "连接WIFI的密码"),
    WIFI_IP_ADDRESS(40117, 2, "连接WIFI本机IP地址"),
    WIFI_SUBNET_MASK(40119, 2, "连接WIFI子网掩码"),
    WIFI_GATEWAY(40121, 2, "连接WIFI网关"),
    WIFI_DNS(40123, 2, "连接WIFI DNS"),
    LOCAL_NETWORK(40125, 8, "本机网络设置(IP地址、子网掩码、网关、DNS)"),
    LOCAL_IP_ADDRESS(-40125, 2, "本机IP地址"),
    LOCAL_SUBNET_MASK(40127, 2, "本机子网掩码"),
    LOCAL_GATEWAY(40129, 2, "本机网关"),
    LOCAL_DNS(40131, 2, "本机DNS"),
    RS485_ADDRESS(40133, 1, "RS485地址"),
    WHITEBOARD_LOCATION(40134, 32, "白板名称（位置）"),
    WHITEBOARD_TITLE(40166, 32, "白板标题"),
    CLASS_CONFIG(40198, 1, "班次配置"),
    ALERT_ENABLE_CONFIGURATION(40199, 1, "预警使能配置"),
    ALERT_UPPER_LIMIT(40200, 8, "预警上限值"),
    ALERT_LOWER_LIMIT(40208, 8, "预警下限值"),
    ALERT_COLOR_CONFIG(40216, 2, "预警颜色配置"),
    WHITEBOARD_THEME(40218, 1, "白板主题"),
    SET_CURRENT_TIME(40425, 2, "设置当前时间"),
    ;

    private final Integer registerAddress;

    private final Integer registerNumber;

    private final String remark;

    private static final Map<Integer, ProtocolTypeEnum> valueMap;

    static {
        valueMap = Arrays.stream(ProtocolTypeEnum.values()).collect(Collectors.toMap(ProtocolTypeEnum::getRegisterAddress, Function.identity()));
    }

    ProtocolTypeEnum(Integer registerAddress, Integer registerNumber, String remark) {
        this.registerAddress = registerAddress;
        this.registerNumber = registerNumber;
        this.remark = remark;
    }

    public Integer getRegisterAddress() {
        return registerAddress;
    }

    public Integer getRegisterNumber() {
        return registerNumber;
    }

    public String getRemark() {
        return remark;
    }

    public static ProtocolTypeEnum fromRegisterAddress(Integer value) {
        return valueMap.get(value);
    }
}
