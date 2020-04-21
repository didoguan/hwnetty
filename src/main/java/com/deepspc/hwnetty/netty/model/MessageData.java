package com.deepspc.hwnetty.netty.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 用于传输数据
 * @Author didoguan
 * @Date 2020/4/19
 **/
@Data
public class MessageData implements Serializable {
	private static final long serialVersionUID = 1806270963949763998L;

	private String id;

	private String msg;

	List<DeviceData> deviceDatas;
}
