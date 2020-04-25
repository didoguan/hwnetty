package com.deepspc.hwnetty.modular.warm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deepspc.hwnetty.modular.warm.entity.EquipmentInfo;
import com.deepspc.hwnetty.modular.warm.mapper.EquipmentInfoMapper;
import com.deepspc.hwnetty.modular.warm.model.QueryParam;
import com.deepspc.hwnetty.modular.warm.service.IEquipmentInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 设备服务实现类
 * @Author didoguan
 * @Date 2020/4/13
 **/
@Service
public class EquipmentInfoService extends ServiceImpl<EquipmentInfoMapper, EquipmentInfo> implements IEquipmentInfoService {
    @Override
    public List<EquipmentInfo> getEquipments(QueryParam queryParam) {
        return this.baseMapper.getEquipments(queryParam);
    }

	@Override
	public EquipmentInfo getEquipmentInfo(Long equipmentId) {
		return this.getById(equipmentId);
	}

}
