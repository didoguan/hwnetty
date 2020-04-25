package com.deepspc.hwnetty.modular.warm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deepspc.hwnetty.modular.warm.entity.EquipmentInfo;
import com.deepspc.hwnetty.modular.warm.model.QueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface EquipmentInfoMapper extends BaseMapper<EquipmentInfo> {
    /**
     * 查询设备
     * @param queryParam 过滤条件
     * @return
     */
    List<EquipmentInfo> getEquipments(@Param("queryParam") QueryParam queryParam);
}
