package com.topsail.im.client.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Steven
 * @date 2021-02-01
 */
@Slf4j
@Component
public class NettyClient implements ApplicationRunner {

    @Value("${netty.server.hostname:127.0.0.1}")
    private String hostname;

    @Value("${netty.server.port:8090}")
    private int port;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
            .group(group)
            // 该参数的作用就是禁止使用 Nagle 算法，使用于小数据即时传输
            .option(ChannelOption.TCP_NODELAY, true)
            .channel(NioSocketChannel.class)
            .handler(new NettyClientInitializer());

        try {
            ChannelFuture future = bootstrap.connect(hostname, port).sync();
            Channel channel = future.channel();
            log.info("客户端成功....");
            channel.writeAndFlush("你好啊");
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
