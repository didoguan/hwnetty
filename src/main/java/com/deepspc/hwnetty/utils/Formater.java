package com.deepspc.hwnetty.utils;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Pattern;

/**
 * @Description 格式处理
 * @Author didoguan
 * @Date 2020/3/1
 **/
public class Formater {

	/**
	 * 判断是否整型
	 * @param str 字符值
	 * @return
	 */
	public static boolean isInteger(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		} else {
			Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
			return pattern.matcher(str).matches();
		}
	}

	/**
	 * 判断是否浮点数(double或float)
	 * @param str 字符值
	 * @return
	 */
	public static boolean isDouble(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		} else {
			Pattern pattern = Pattern.compile("^[-\\+]?\\d*[.]\\d+$");
			return pattern.matcher(str).matches();
		}
	}
}
