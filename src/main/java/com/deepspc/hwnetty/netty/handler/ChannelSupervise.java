package com.deepspc.hwnetty.netty.handler;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Description 查找某一个客户端的通道时候必须通过channelId对象去查找
 * 而channelId不能人为创建，所有必须通过map将channelId的字符串和channel保存起来
 * @Author didoguan
 * @Date 2020/4/19
 **/
public class ChannelSupervise {

	private static ChannelGroup GlobalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private static ConcurrentMap<String, Channel> ChannelMap = new ConcurrentHashMap();

	public static void addChannel(Channel channel){
		GlobalGroup.add(channel);
	}

	public static void removeChannel(Channel channel){
		GlobalGroup.remove(channel);
		AttributeKey<String> key = AttributeKey.valueOf("clientId");
		String id = channel.attr(key).get();
		if (StrUtil.isNotBlank(id)) {
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
	public static void sendToClient(String id, TextWebSocketFrame tws) {
		Channel channel = ChannelMap.get(id);
		channel.writeAndFlush(tws);
	}
}
