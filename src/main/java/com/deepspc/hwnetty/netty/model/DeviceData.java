package com.deepspc.hwnetty.netty.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 设备信息
 * @Author didoguan
 * @Date 2020/4/19
 **/
@Data
public class DeviceData implements Serializable {
	//唯一码
	private String uniqueNo;
	//序列号
	private String serialNo;
	//功率
	private Integer watts;
	//温度
	private Float temperature;
	//湿度
	private Integer humidity;
	//状态 0-故障 1-正常
	private Integer status;
	//设备类型 1-主机 2-温度执行器
	private Integer deviceType;
    //开始时间
    private String startTime;
    //结束时间
    private String endTime;
}
