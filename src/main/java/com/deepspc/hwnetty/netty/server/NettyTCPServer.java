package com.deepspc.hwnetty.netty.server;

import com.deepspc.hwnetty.netty.handler.DeviceServerHandler;
import com.deepspc.hwnetty.netty.handler.SwitchProtocolHandle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Description netty服务端
 * @Author didoguan
 * @Date 2020/2/27
 **/
@Component
public class NettyTCPServer {

    private Logger log = LoggerFactory.getLogger(NettyTCPServer.class);

    @Autowired
	private DeviceServerHandler deviceServerHandler;

    @Value("${server.port}")
    private int port;

    @PostConstruct
    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
					//Socket 连接心跳检测
					socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(60, 0, 0));
					socketChannel.pipeline().addLast("switchProtocolHandle", new SwitchProtocolHandle());
					//注意，这个专门针对 Socket 信息的解码器只能放在 SwitchProtocolHandle 之后，否则会导致 webSocket 连接出错
					//socketChannel.pipeline().addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
					//socketChannel.pipeline().addLast("lengthEncode", new LengthFieldPrepender(4));
					socketChannel.pipeline().addLast("commonhandler", deviceServerHandler);
					}
				})
				//可连接的客户端队列大小
                .option(ChannelOption.SO_BACKLOG, 1024)
				//保持连接，2小时内无数据通信TCP发送探测报文
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				//允许重复使用本地地址和端口
				.childOption(ChannelOption.SO_REUSEADDR, true)
				/*一个连接的远端关闭时本地端是否关闭，默认值为False。值为False时，连接自动关闭；
				 *为True时，触发ChannelInboundHandler的userEventTriggered()方法，
				 *事件为ChannelInputShutdownEvent
				 */
				.childOption(ChannelOption.ALLOW_HALF_CLOSURE, true);
        try {
            //异步创建服务绑定
            ChannelFuture cf = b.bind(port).sync();
            log.info("==========开启Netty服务=========");
            //关闭服务通道
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        } finally {
            //释放线程池资源
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }
}
