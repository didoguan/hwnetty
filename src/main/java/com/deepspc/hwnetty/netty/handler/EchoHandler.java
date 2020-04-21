package com.deepspc.hwnetty.netty.handler;

import com.deepspc.hwnetty.core.enums.BizEnum;
import com.deepspc.hwnetty.modular.gate.entity.Temperature;
import com.deepspc.hwnetty.modular.gate.service.ITemperatureService;
import com.deepspc.hwnetty.utils.Formater;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Date;

/**
 * @Description channel处理
 * @Author didoguan
 * @Date 2020/2/28
 **/
@Component
@ChannelHandler.Sharable
public class EchoHandler extends ChannelInboundHandlerAdapter {

    private Logger log = LoggerFactory.getLogger(EchoHandler.class);

    @Autowired
    private ITemperatureService temperatureService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        String dataStr = in.toString(CharsetUtil.UTF_8);
        log.info("Netty读取到的数据：{}", dataStr);
        boolean isFloat = Formater.isDouble(dataStr);
        if (isFloat) {
			Temperature temp = new Temperature();
			float degree = Float.parseFloat(dataStr);
			temp.setTempNumber(degree);
			temp.setCreateTime(new Date());
			//temperatureService.addTemperature(temp);
			ctx.writeAndFlush(BizEnum.SUCCESS);
		} else {
        	ctx.writeAndFlush(BizEnum.FORMAT_ERR);
		}
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ChannelSupervise.addChannel(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		InetSocketAddress reAddr = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIP = reAddr.getAddress().getHostAddress();
		String clientPort = String.valueOf(reAddr.getPort());
		log.info("socket连接断开："+ clientIP +":"+ clientPort);
		ChannelSupervise.removeChannel(ctx.channel());
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.READER_IDLE) {
				// 在规定时间内没有收到客户端的上行数据, 主动断开连接
				ctx.disconnect();
				log.info("心跳检测触发，socket连接断开！");
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
