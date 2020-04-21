package com.deepspc.hwnetty.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * @Description json工具类
 * @Author didoguan
 * @Date 2020/4/19
 **/
public class JsonUtil {

	public static String obj2json(Object o) {
		if (null != o) {
			return JSON.toJSONString(o);
		} else {
			return null;
		}
	}

	public static <T> T json2obj(String json, Class<T> c) {
		return JSON.parseObject(json, c);
	}

	public static <T> List<T> json2list(String json, Class<T> c) {
		return JSON.parseArray(json, c);
	}

	public static <T> Map<String, T> json2map(String json) {
		return JSONObject.parseObject(json, new TypeReference<Map<String, T>>(){});
	}
}
