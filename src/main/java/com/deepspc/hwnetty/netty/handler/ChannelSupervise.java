package com.deepspc.hwnetty.netty.handler;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Description 查找某一个客户端的通道时候必须通过channelId对象去查找
 * 而channelId不能人为创建，所有必须通过map将channelId的字符串和channel保存起来
 * @Author didoguan
 * @Date 2020/4/19
 **/
public class ChannelSupervise {

	private static Logger log = LoggerFactory.getLogger(ChannelSupervise.class);

	private static ChannelGroup GlobalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private static ConcurrentMap<String, Channel> ChannelMap = new ConcurrentHashMap();

	public static void addChannel(Channel channel){
		GlobalGroup.add(channel);
		AttributeKey<String> key = AttributeKey.valueOf("clientId");
		String id = channel.attr(key).get();
		if (StrUtil.isNotBlank(id)) {
		    log.info("添加到ChannelMap中的channel为：{}", id);
			ChannelMap.put(id, channel);
		}
	}

	public static void removeChannel(Channel channel){
		GlobalGroup.remove(channel);
		AttributeKey<String> key = AttributeKey.valueOf("clientId");
		String id = channel.attr(key).get();
		if (StrUtil.isNotBlank(id)) {
            log.info("删除ChannelMap中的channel为：{}", id);
			ChannelMap.remove(id);
		}
	}

	public static Channel findChannel(String key){
		return ChannelMap.get(key);
	}

	public static ConcurrentMap<String, Channel> getChannelMap() {
		return ChannelMap;
	}

	/**
	 * 群发
	 */
	public static void sendToAll(TextWebSocketFrame tws){
		GlobalGroup.writeAndFlush(tws);
	}

	/**
	 * 发送数据到指定客户端
	 */
	public static void sendToClient(String id, Object tws) {
	    log.info("发送数据到channel:{}", id);
		Channel channel = ChannelMap.get(id);
		if (null != channel) {
			channel.writeAndFlush(tws);
		} else {
			log.error("终端设备推送信息异常！找不到对应通道，可能已经断开连接。");
		}
	}
}
