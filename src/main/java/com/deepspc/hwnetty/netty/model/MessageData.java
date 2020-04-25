package com.deepspc.hwnetty.netty.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 终端传到app的实时数据
 * @Author didoguan
 * @Date 2020/4/19
 **/
@Data
public class MessageData implements Serializable {
	private static final long serialVersionUID = 1806270963949763998L;

    /**
     * key值需要进行约定，websocket为设备唯一码加下划线加wsk，
     * socket为设备唯一码加下划线加sk
     */
	private String id;

	private String msg;

	List<DeviceData> deviceDatas;
}
