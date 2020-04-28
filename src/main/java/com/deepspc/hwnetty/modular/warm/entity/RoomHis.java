package com.deepspc.hwnetty.modular.warm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 房间温湿度历史
 * @Author didoguan
 * @Date 2020/4/6
 **/
@TableName("wm_room_his")
@Data
public class RoomHis implements Serializable {
	private static final long serialVersionUID = 4733946210972680478L;

	public RoomHis() {

	}

	@TableId(value = "his_id", type = IdType.ID_WORKER)
	private Long hisId;

	@TableField("room_id")
	private Long roomId;

	@TableField("unique_no")
	private String uniqueNo;

	@TableField("serial_no")
	private String serialNo;

	@TableField("temperature")
	private Float temperature;

	@TableField("humidity")
	private Integer humidity;

	@TableField("create_time")
	private Date createTime;

	@TableField(exist = false)
	private String createTimeStr;
}
