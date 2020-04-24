package com.deepspc.hwnetty.netty.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 响应数据
 * @Author didoguan
 * @Date 2020/4/24
 **/
@Data
public class ResponseData implements Serializable {
    private static final long serialVersionUID = -5455896883625577728L;

    private String code;

    private String msg;

    private Object data;

    public ResponseData() {

    }
}
