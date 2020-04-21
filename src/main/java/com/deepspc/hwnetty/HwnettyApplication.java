package com.deepspc.hwnetty;

import com.deepspc.hwnetty.netty.server.NettyTCPServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HwnettyApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(HwnettyApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);//不启动web服务
		app.run(args);
		//启动Netty服务
		new NettyTCPServer();
	}

}
