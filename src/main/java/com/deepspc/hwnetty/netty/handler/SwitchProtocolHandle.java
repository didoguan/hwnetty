package com.deepspc.hwnetty.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;

/**
 * @Description 根据请求判断使用socket还是websocket
 * @Author didoguan
 * @Date 2020/4/19
 **/
public class SwitchProtocolHandle extends ByteToMessageDecoder {

	/**
	 * 默认暗号长度
	 */
	private static final int MAX_LENGTH = 23;

	/**
	 * websocket握手的协议前缀
	 */
	private static final String WEBSOCKET_PREFIX = "GET /";

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
		String protocol = getBufStart(in);
		if (protocol.startsWith(WEBSOCKET_PREFIX)) {
			PipelineAdd pipelineAdd = new PipelineAdd();
			pipelineAdd.websocketAdd(ctx);
			//对于 webSocket ，不设置超时断开
			ctx.pipeline().remove(IdleStateHandler.class);
			//ctx.pipeline().remove(LengthFieldBasedFrameDecoder.class);
			//ctx.pipeline().remove(LengthFieldPrepender.class);
		}
		in.resetReaderIndex();
		ctx.pipeline().remove(this.getClass());
	}

	private String getBufStart(ByteBuf in){
		int length = in.readableBytes();
		if (length > MAX_LENGTH) {
			length = MAX_LENGTH;
		}
		//标记读位置
		in.markReaderIndex();
		byte[] content = new byte[length];
		in.readBytes(content);
		return new String(content);
	}
}
