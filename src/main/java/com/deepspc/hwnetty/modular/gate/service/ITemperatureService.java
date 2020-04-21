package com.deepspc.hwnetty.modular.gate.service;

import com.deepspc.hwnetty.modular.gate.entity.Temperature;

/**
 * 温度记录服务接口
 */
public interface ITemperatureService {

	boolean addTemperature(Temperature temperature);
}
