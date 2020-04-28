package com.deepspc.hwnetty.netty.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.deepspc.hwnetty.core.constant.BizConstant;
import com.deepspc.hwnetty.modular.warm.entity.EquipmentInfo;
import com.deepspc.hwnetty.modular.warm.entity.RoomHis;
import com.deepspc.hwnetty.modular.warm.entity.RoomInfo;
import com.deepspc.hwnetty.modular.warm.mapper.EquipmentInfoMapper;
import com.deepspc.hwnetty.modular.warm.mapper.RoomInfoMapper;
import com.deepspc.hwnetty.modular.warm.service.IRoomHisService;
import com.deepspc.hwnetty.netty.model.DeviceData;
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
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Resource
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private IRoomHisService roomHisService;

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
     * 2.netty保存数据
	 * @param ctx
	 * @param msg
	 */
	private void socketMsgHandle(ChannelHandlerContext ctx, ByteBuf msg) {
		String dataStr = msg.toString(CharsetUtil.UTF_8);
		//终端连接时必须发送数据到服务端
		if (StrUtil.isNotBlank(dataStr)) {
			MessageData messageData = JsonUtil.json2obj(dataStr, MessageData.class);
			String id = messageData.getId();
			setClientId(ctx, id);
			//添加到Channel组
			ChannelSupervise.addChannel(ctx.channel());
			String subId = id.split("_")[0];
			String suffix = id.split("_")[1];
			String key = "";
			if (SOCKET_SUFFIX.equals(suffix)) {
			    /* 1.来源为终端硬件，保存数据为历史数据
			     * 2.主动推送到前端app
			     */
                key = subId + WEBSOCKET_SUFFIX;
                List<DeviceData> datas = messageData.getDeviceDatas();
                //传送到前端
                TextWebSocketFrame tws = new TextWebSocketFrame(JsonUtil.obj2json(datas));
                ChannelSupervise.sendToClient(key, tws);
                /**********保存数据到历史表*********/
                List<RoomHis> hisDatas = new ArrayList<>(16);
                for (DeviceData data : datas) {
                    if (data.getDeviceType().intValue() == 1) {
                        continue;
                    }
                    RoomHis rm = new RoomHis();
                    rm.setUniqueNo(data.getUniqueNo());
                    rm.setSerialNo(data.getSerialNo());
                    rm.setTemperature(data.getTemperature());
                    rm.setHumidity(data.getHumidity());
                    hisDatas.add(rm);
                    //更新房间状态
                    roomInfoMapper.updateRoomInfoStatus(data.getStatus(), data.getUniqueNo(), data.getSerialNo());
                }
                roomHisService.saveBatch(hisDatas);
            } else if (SPRINGBOOT_SUFFIX.equals(suffix)) {
			    //来源为spring服务器，传输数据到终端硬件
                key = subId + SOCKET_SUFFIX;
                ChannelSupervise.sendToClient(key, msg);
            }
		} else {
            MessageData messageData = new MessageData();
            messageData.setId("400");
            messageData.setMsg("服务器没收到传输数据");
            String str = JsonUtil.obj2json(messageData);
            try {
                byte[] strByte = str.getBytes("UTF-8");
                ByteBuf btu = Unpooled.wrappedBuffer(strByte);
                //传到终端设备进行设置
                ctx.channel().writeAndFlush(btu);
            } catch(UnsupportedEncodingException e) {
                e.printStackTrace();
            }
		}
	}

	/**
	 * 处理websocket请求<br>
     * 1.app发送连接请求，netty只需保存websocket的通道。
     * 1.传入的数据code必须为客户标识
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
        ResponseData respWsk = new ResponseData();
		if (StrUtil.isNotBlank(wskStr)) {
            ResponseData wskData = JsonUtil.json2obj(wskStr, ResponseData.class);
            Long customerId = Long.valueOf(wskData.getCode());
            QueryWrapper<EquipmentInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("customer_id", customerId);
            queryWrapper.eq("equipment_type", 1);
            EquipmentInfo equipmentInfo = equipmentInfoMapper.selectOne(queryWrapper);
            if (null != equipmentInfo) {
                setClientId(ctx, equipmentInfo.getUniqueNo() + WEBSOCKET_SUFFIX);
                ChannelSupervise.addChannel(ctx.channel());
            }

            /**************响应前端连接************/
            respWsk.setCode("200");
            respWsk.setMsg("已接收到请求");
        } else {
            respWsk.setCode("201");
            respWsk.setMsg("已连接，但无传入数据！");
        }
        TextWebSocketFrame respStr = new TextWebSocketFrame(JsonUtil.obj2json(respWsk));
        ctx.channel().writeAndFlush(respStr);
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
