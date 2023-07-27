package com.bdtd.jd4.common;

/**
 * @author hang.lv
 * @version 1.0.0
 * @since 2022/12/15 14:56
 */
public class ProtocolResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 协议类型，响应成功时返回
     */
    private ProtocolTypeEnum protocolTypeEnum;

    public ProtocolResponse() {
    }

    public ProtocolResponse(boolean success, String errorCode, String errorMessage, ProtocolTypeEnum protocolTypeEnum) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.protocolTypeEnum = protocolTypeEnum;
    }

    /**
     * 成功响应
     *
     * @return ProtocolResponse
     */
    public static ProtocolResponse success() {
        return new ProtocolResponse(true, null, null, null);
    }

    /**
     * 成功响应
     *
     * @param protocolTypeEnum 协议类型
     * @return ProtocolResponse
     */
    public static ProtocolResponse success(ProtocolTypeEnum protocolTypeEnum) {
        return new ProtocolResponse(true, null, null, protocolTypeEnum);
    }

    /**
     * 异常响应
     *
     * @return ProtocolResponse
     */
    public static ProtocolResponse error() {
        return new ProtocolResponse(false, null, null, null);
    }

    /**
     * 异常响应
     *
     * @param errorCode    错误码
     * @param errorMessage 错误信息
     * @return ProtocolResponse
     */
    public static ProtocolResponse error(String errorCode, String errorMessage) {
        return new ProtocolResponse(false, errorCode, errorMessage, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ProtocolTypeEnum getProtocolTypeEnum() {
        return protocolTypeEnum;
    }

    public void setProtocolTypeEnum(ProtocolTypeEnum protocolTypeEnum) {
        this.protocolTypeEnum = protocolTypeEnum;
    }

    @Override
    public String toString() {
        return "ProtocolResponse{" + "success=" + success +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", protocolTypeEnum=" + protocolTypeEnum +
                '}';
    }
}
