package com.deepspc.hwnetty.modular.warm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deepspc.hwnetty.modular.warm.entity.EquipmentInfo;
import com.deepspc.hwnetty.modular.warm.model.QueryParam;

import java.util.List;

/**
 * 设备信息接口
 */
public interface IEquipmentInfoService extends IService<EquipmentInfo> {

    /**
     * 查询设备
     * @param queryParam 过滤条件
     * @return
     */
    List<EquipmentInfo> getEquipments(QueryParam queryParam);

	/**
	 * 获取设备详情
	 * @param equipmentId 设备标识
	 * @return
	 */
	EquipmentInfo getEquipmentInfo(Long equipmentId);

}
