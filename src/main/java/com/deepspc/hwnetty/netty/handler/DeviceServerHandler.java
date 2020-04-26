package com.deepspc.hwnetty.netty.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.deepspc.hwnetty.core.constant.BizConstant;
import com.deepspc.hwnetty.modular.warm.entity.EquipmentInfo;
import com.deepspc.hwnetty.modular.warm.mapper.EquipmentInfoMapper;
import com.deepspc.hwnetty.modular.warm.model.QueryParam;
import com.deepspc.hwnetty.netty.model.DeviceSetData;
import com.deepspc.hwnetty.netty.model.MessageData;
import com.deepspc.hwnetty.netty.model.ResponseData;
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

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

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
	@Resource
	private EquipmentInfoMapper equipmentInfoMapper;

	private WebSocketServerHandshaker handshaker;

	private final String WEBSOCKET_SUFFIX = "_wsk";
    private final String SOCKET_SUFFIX = "_sk";
    //来源为springboot服务器
    private final String SPRINGBOOT_SUFFIX = "_spb";

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
			socketMsgHandle(ctx, buff);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		InetSocketAddress reAddr = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIP = reAddr.getAddress().getHostAddress();
		String clientPort = String.valueOf(reAddr.getPort());
		log.info("创建连接："+ clientIP +":"+ clientPort);
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

	/**
	 * 处理socket请求<br>
     * 1.终端设备发送硬件实时数据，netty查找websocket通道再发送到app显示。
     * 2.终端设备接收app设置的数据，终端完成设置后发送状态告诉服务器是否设置完成，
     * code为200表示设置完成，400表示设置失败。
	 * @param ctx
	 * @param msg
	 */
	private void socketMsgHandle(ChannelHandlerContext ctx, ByteBuf msg) {
		String dataStr = msg.toString(CharsetUtil.UTF_8);
        TextWebSocketFrame tws = null;
        ResponseData resp = new ResponseData();
		//终端连接时必须发送数据到服务端
		if (StrUtil.isNotBlank(dataStr)) {
			MessageData messageData = JsonUtil.json2obj(dataStr, MessageData.class);
			String id = messageData.getId();
			setClientId(ctx, id);
			//添加到Channel组
			ChannelSupervise.addChannel(ctx.channel());
			String subId = id.split("_")[0];
			String key = subId + WEBSOCKET_SUFFIX;
			resp.setCode("200");
			resp.setData(messageData.getDeviceDatas());
			//推送信息到前端app
            tws = new TextWebSocketFrame(JsonUtil.obj2json(resp));
			ChannelSupervise.sendToClient(key, tws);

			List<DeviceSetData> devices = new ArrayList<>(16);
			DeviceSetData setData = new DeviceSetData();
			setData.setCustomerId(12345l);
			setData.setEndTime("18:00");
			setData.setStartTime("08:00");
			setData.setTemperature(18.5f);
			setData.setSerialNo("12345678");
			devices.add(setData);
			resp.setData(devices);
		} else {
		    resp.setCode("400");
		    resp.setMsg("服务器没有接收到任何数据");
		}
		try {
			byte[] strByte = JSON.toJSONString(resp).getBytes("UTF-8");
			ByteBuf btu = Unpooled.wrappedBuffer(strByte);
			ctx.channel().writeAndFlush(btu);
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理websocket请求<br>
     * 1.app发送连接请求，netty保存通道。code为300表示请求实时数据显示，
     * code为301表示请求设置硬件数据。
     * 2.netty接收到请求设置硬件数据时获取终端设备通道，传送设置数据
	 * @param ctx
	 * @param frame
	 */
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

		String wskStr = ((TextWebSocketFrame) frame).text();
		if (StrUtil.isNotBlank(wskStr)) {
			List<DeviceSetData> datas = JsonUtil.json2list(wskStr, DeviceSetData.class);
			Long customerId = datas.get(0).getCustomerId();
			//根据customerId获取对应的主机唯一码
            QueryParam queryParam = new QueryParam();
            queryParam.setCustomerId(customerId);
            queryParam.setEquipmentType(1);
            List<EquipmentInfo> equipmentInfos = equipmentInfoMapper.getEquipments(queryParam);
            if (null != equipmentInfos && !equipmentInfos.isEmpty()) {
                EquipmentInfo equipmentInfo = equipmentInfos.get(0);
                String uniqueNo = equipmentInfo.getUniqueNo();
                setClientId(ctx, uniqueNo + WEBSOCKET_SUFFIX);
                ChannelSupervise.addChannel(ctx.channel());
                String skId = uniqueNo + SOCKET_SUFFIX;
                ResponseData resp = new ResponseData();
                resp.setCode("200");
                resp.setMsg("来自客户端的数据");
                resp.setData(wskStr);
                try {
                    byte[] strByte = wskStr.getBytes("UTF-8");
                    ByteBuf btu = Unpooled.wrappedBuffer(strByte);
                    //传到终端设备进行设置
                    ChannelSupervise.sendToClient(skId, btu);
                } catch(UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                ResponseData resp = new ResponseData();
                resp.setCode("400");
                resp.setMsg("找不到对应的设备编码");
                TextWebSocketFrame tws = new TextWebSocketFrame(JsonUtil.obj2json(resp));
                ctx.channel().writeAndFlush(tws);
            }
		}
	}

	/**
	 * 处理http请求
	 * @param ctx
	 * @param req
	 */
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

	/**
	 * 设置channel中clientId属性值
	 * @param val
	 */
	private void setClientId(ChannelHandlerContext ctx, String val) {
		//将客户端ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
		AttributeKey<String> key = AttributeKey.valueOf("clientId");
		ctx.channel().attr(key).setIfAbsent(val);
	}
}
