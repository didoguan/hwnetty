package com.deepspc.hwnetty.netty.handler;

import com.deepspc.hwnetty.core.enums.BizEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @Description 设备交互通道事件处理
 * @Author didoguan
 * @Date 2020/4/21
 **/
@Component
@ChannelHandler.Sharable
public class DeviceChannelHandler extends ChannelInboundHandlerAdapter {

	private Logger log = LoggerFactory.getLogger(DeviceServerHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf in = (ByteBuf) msg;
		String dataStr = in.toString(CharsetUtil.UTF_8);
		log.info("Netty读取到的数据：{}", dataStr);

		ctx.writeAndFlush(BizEnum.SUCCESS);
		ctx.flush();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info(">>>>>>>>>创建连接>>>>>>>>>>");
		ChannelSupervise.addChannel(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info(">>>>>>>>>连接断开>>>>>>>>>>");
		ChannelSupervise.removeChannel(ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ChannelSupervise.removeChannel(ctx.channel());
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.READER_IDLE) {
				// 在规定时间内没有收到客户端的上行数据, 主动断开连接
				ctx.disconnect();
				log.info(">>>>>>>>>>>>心跳检测触发，连接断开！>>>>>>>>>>>>>>");
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
