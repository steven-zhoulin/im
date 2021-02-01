package com.topsail.im.server;

import com.topsail.im.server.register.EndpointRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @author Steven
 * @date 2021-02-01
 */
@Slf4j
@Component
public class PushServer implements ApplicationRunner {

    @Value("${netty.server.hostname:127.0.0.1}")
    private String hostname;

    @Value("${netty.server.port:8090}")
    private int port;

    @Autowired
    private EndpointRegistry endpointRegistry;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(200);
        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup, workGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ServerChannelInitializer())
            .localAddress(inetSocketAddress)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            ChannelFuture future = bootstrap.bind(inetSocketAddress).sync();
            log.info("服务器启动开始监听端口: {}", inetSocketAddress.getPort());
            String endpoint = hostname + ":" + port;

            endpointRegistry.online(endpoint);
            log.info("上报 PushServer 接入地址: {}", endpoint);

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
