package com.deepspc.hwnetty.modular.warm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 房间信息
 * @Author didoguan
 * @Date 2020/4/6
 **/
@TableName("wm_room_info")
@Data
public class RoomInfo implements Serializable {
	private static final long serialVersionUID = -155984488291487436L;

	@TableId(value = "room_id", type = IdType.ID_WORKER)
	private Long roomId;

	@TableField("room_name")
	private String roomName;

    @TableField("model_id")
	private Long modelId;

	@TableField("unique_no")
	private String uniqueNo;

	@TableField("serial_no")
	private String serialNo;

	@TableField("temperature")
	private Float temperature;

	@TableField("start_time")
	private String startTime;

	@TableField("end_time")
	private String endTime;

	@TableField("status")
	private Integer status;

	@TableField("runing_status")
	private Integer runingStatus;

	@TableField("customer_id")
	private Long customerId;

	@TableField("icon_id")
	private Long iconId;

	@TableField("icon_path")
	private String iconPath;

	public RoomInfo() {

	}
}
