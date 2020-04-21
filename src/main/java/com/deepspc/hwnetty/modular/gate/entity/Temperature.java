package com.deepspc.hwnetty.modular.gate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @Description 温度信息
 * @Author didoguan
 * @Date 2020/3/1
 **/
@TableName("t_temperature")
@Data
public class Temperature {

	@TableId(value = "temperature_id", type = IdType.ID_WORKER)
	private Long temperatureId;

	@TableField("gate_id")
	private Long gateId;

	@TableField("temp_number")
	private Float tempNumber;

	@TableField(value = "create_time")
	private Date createTime;
}
