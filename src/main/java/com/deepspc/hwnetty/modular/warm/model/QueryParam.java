package com.deepspc.hwnetty.modular.warm.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 数据查询参数
 * @Author didoguan
 * @Date 2020/4/13
 **/
@Data
public class QueryParam implements Serializable {
    private static final long serialVersionUID = -6846570322373326967L;

    private String equipmentName;

    private Long customerId;

	private Integer equipmentType;
}
