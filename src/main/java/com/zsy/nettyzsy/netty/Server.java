package com.zsy.nettyzsy.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * Netty服务端
 *
 * @author gaoyi
 *
 */
@Component
public class Server {

    private ServerSocketChannel serverSocketChannel;

    public void bind(final int serverPort) throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 服务端要建立两个group，一个负责接收客户端的连接，一个负责处理数据传输
                // 连接处理group
                EventLoopGroup boss = new NioEventLoopGroup();
                // 事件处理group
                EventLoopGroup worker = new NioEventLoopGroup(10);
                ServerBootstrap bootstrap = new ServerBootstrap();
                // 绑定处理group
                // bootstrap.group(boss,
                // worker).channel(NioServerSocketChannel.class).handler(new
                // LoggingHandler(LogLevel.DEBUG))
                bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                        // 保持连接数
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        // 有数据立即发送
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        // 保持连接
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                                new WriteBufferWaterMark(32 * 1024, 100 * 1024 * 1024))
                        // 处理新连接
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel sc) throws Exception {
                                // 增加任务处理
                                ChannelPipeline p = sc.pipeline();
                                p.addLast(new IdleStateHandler(5, 0, 0, TimeUnit.MINUTES),
                                        new DelimiterBasedFrameDecoder(1024 * 1024,
                                                Unpooled.copiedBuffer("$$__".getBytes())),
                                        // 自定义的处理器
                                        new ServerHandler());
                            }
                        });

                // 绑定端口，同步等待成功
                ChannelFuture future;
                try {
                    future = bootstrap.bind(serverPort).sync();
                    if (future.isSuccess()) {
                        serverSocketChannel = (ServerSocketChannel) future.channel();
                        System.out.println("服务端开启成功....................");
                    } else {
                        System.out.println("服务端开启失败....................");
                    }
                    // 等待服务监听端口关闭,就是由于这里会将线程阻塞，导致无法发送信息，所以我这里开了线程
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    System.out.println("启动失败！"+e);
                } finally {
                    System.out.println("优雅的关闭服务端................................");
                    // 优雅地退出，释放线程池资源
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                }
            }
        });
        thread.start();
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            //handler
            ch.pipeline().addLast(new ServerHandler());

        }

    }
}