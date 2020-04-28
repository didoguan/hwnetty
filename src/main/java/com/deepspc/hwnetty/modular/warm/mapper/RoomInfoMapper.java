package com.deepspc.hwnetty.modular.warm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deepspc.hwnetty.modular.warm.entity.RoomInfo;
import org.apache.ibatis.annotations.Param;

public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    /**
     * 更新状态
     */
    void updateRoomInfoStatus(@Param("status") Integer status, @Param("uniqueNo") String uniqueNo, @Param("serialNo") String serialNo);
}
