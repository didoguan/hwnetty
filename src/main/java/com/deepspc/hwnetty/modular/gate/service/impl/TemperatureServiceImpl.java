package com.deepspc.hwnetty.modular.gate.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deepspc.hwnetty.modular.gate.entity.Temperature;
import com.deepspc.hwnetty.modular.gate.mapper.TemperatureMapper;
import com.deepspc.hwnetty.modular.gate.service.ITemperatureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Description 温度接口实现类
 * @Author didoguan
 * @Date 2020/3/1
 **/
@EnableTransactionManagement
@Service
public class TemperatureServiceImpl extends ServiceImpl<TemperatureMapper, Temperature> implements ITemperatureService {
	@Override
	public boolean addTemperature(Temperature temperature) {
		return this.save(temperature);
	}
}
