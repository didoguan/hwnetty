package com.deepspc.hwnetty.netty.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 向终端传送数据对象
 * @Author didoguan
 * @Date 2020/4/23
 **/
@Data
public class ResponseDeviceData implements Serializable {
    private static final long serialVersionUID = -876983669125082693L;

    //唯一码
    private String uniqueNo;
    //序列号
    private String serialNo;
    //温度
    private Float temperature;
    //开始时间
    private String startTime;
    //结束时间
    private String endTime;

    public ResponseDeviceData() {

    }
}
