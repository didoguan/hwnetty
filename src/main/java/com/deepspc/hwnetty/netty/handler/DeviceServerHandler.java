package com.deepspc.hwnetty.netty.handler;

import cn.hutool.core.util.StrUtil;
import com.deepspc.hwnetty.core.constant.BizConstant;
import com.deepspc.hwnetty.netty.model.MessageData;
import com.deepspc.hwnetty.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Date;

import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;

/**
 * @Description 处理socket和websocket信息
 * @Author didoguan
 * @Date 2020/4/19
 **/
@Component
@ChannelHandler.Sharable
public class DeviceServerHandler extends SimpleChannelInboundHandler<Object> {

	private Logger log = LoggerFactory.getLogger(DeviceServerHandler.class);

	private WebSocketServerHandshaker handshaker;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buff = (ByteBuf) msg;
		String info = buff.toString(CharsetUtil.UTF_8);
		log.info("收到消息内容："+info);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    System.out.println("========读取数据=======");
		//websocket消息处理
		if (msg instanceof WebSocketFrame) {
			String webSocketInfo = ((TextWebSocketFrame) msg).text().trim();
			log.info("收到webSocket消息：" + webSocketInfo);
			websocketMsgHandle(ctx, (WebSocketFrame) msg);
		}
		//以http请求形式接入，但是走的是websocket
		else if (msg instanceof FullHttpRequest) {
			httpMsgRequest(ctx, (FullHttpRequest) msg);
			log.info("收到http请求");
		}
		//socket消息处理
		else {
			ByteBuf buff = (ByteBuf) msg;
			String socketInfo = buff.toString(CharsetUtil.UTF_8).trim();
			log.info("收到socket消息："+socketInfo);
		}
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
		log.info("连接断开："+ clientIP +":"+ clientPort);
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
				log.info("超过读空闲时间，连接断开！");
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	private void websocketMsgHandle(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// 判断是否关闭链路的指令
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		// 判断是否ping消息
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}

		String resp = ((TextWebSocketFrame) frame).text();
		if (StrUtil.isNotBlank(resp)) {
			MessageData messageData = JsonUtil.json2obj(resp, MessageData.class);
			String id = messageData.getId();
			ChannelSupervise.getChannelMap().put(id, ctx.channel());
			/*将客户端ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
			*AttributeKey<String> key = AttributeKey.valueOf("clientId");
			*ctx.channel().attr(key).setIfAbsent(id);
			*/
		}
		//返回信息
		ctx.channel().writeAndFlush("{\"code\":\"200\",\"message\":\"服务器连接成功\"}\r\n");
	}

	private void httpMsgRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
			//若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(BizConstant.WST_URL, null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}

	/**
	 * 拒绝不合法请求
	 */
	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
		// 返回应答给客户端
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
		}
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		// 如果是非Keep-Alive，关闭连接
		if (!isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
}
