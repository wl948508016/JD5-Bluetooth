package com.bdtd.ccg.common;

/**
 * @author hang.lv
 * @version 1.0.0
 * @since 2022/11/28 11:37
 */
public class BizTaskDataProtocol {

    /** 帧头 */
    private String frameHeader;

    /** 任务ID */
    private Long taskPeopleId;

    /** 班次ID */
    private Long classId;

    /** 巡检次数 */
    private Integer checkTimes;

    /** 巡检时间：yyyy-MM-dd HH:mm:ss */
    private String checkTime;

    /** 巡检员名称 */
    private String workUserName;

    /** 甲烷值 */
    private String methaneVal;

    /** 氧气值 */
    private String oxygenVal;

    /** 一氧化碳值 */
    private String carbonMonoxideVal;

    /** 二氧化碳值 */
    private String carbonDioxideVal;

    /** 温度值 */
    private String temperatureVal;

    public String getFrameHeader() {
        return frameHeader;
    }

    public void setFrameHeader(String frameHeader) {
        this.frameHeader = frameHeader;
    }

    public Long getTaskPeopleId() {
        return taskPeopleId;
    }

    public void setTaskPeopleId(Long taskPeopleId) {
        this.taskPeopleId = taskPeopleId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Integer getCheckTimes() {
        return checkTimes;
    }

    public void setCheckTimes(Integer checkTimes) {
        this.checkTimes = checkTimes;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    public String getWorkUserName() {
        return workUserName;
    }

    public void setWorkUserName(String workUserName) {
        this.workUserName = workUserName;
    }

    public String getMethaneVal() {
        return methaneVal;
    }

    public void setMethaneVal(String methaneVal) {
        this.methaneVal = methaneVal;
    }

    public String getOxygenVal() {
        return oxygenVal;
    }

    public void setOxygenVal(String oxygenVal) {
        this.oxygenVal = oxygenVal;
    }

    public String getCarbonMonoxideVal() {
        return carbonMonoxideVal;
    }

    public void setCarbonMonoxideVal(String carbonMonoxideVal) {
        this.carbonMonoxideVal = carbonMonoxideVal;
    }

    public String getCarbonDioxideVal() {
        return carbonDioxideVal;
    }

    public void setCarbonDioxideVal(String carbonDioxideVal) {
        this.carbonDioxideVal = carbonDioxideVal;
    }

    public String getTemperatureVal() {
        return temperatureVal;
    }

    public void setTemperatureVal(String temperatureVal) {
        this.temperatureVal = temperatureVal;
    }

    @Override
    public String toString() {
        return "BizTaskDataProtocol{" + "frameHeader='" + frameHeader + '\'' +
                ", taskPeopleId=" + taskPeopleId +
                ", classId=" + classId +
                ", checkTimes=" + checkTimes +
                ", checkTime='" + checkTime + '\'' +
                ", workUserName='" + workUserName + '\'' +
                ", methaneVal='" + methaneVal + '\'' +
                ", oxygenVal='" + oxygenVal + '\'' +
                ", carbonMonoxideVal='" + carbonMonoxideVal + '\'' +
                ", carbonDioxideVal='" + carbonDioxideVal + '\'' +
                ", temperatureVal='" + temperatureVal + '\'' +
                '}';
    }
}
