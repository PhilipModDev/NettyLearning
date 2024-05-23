package org.example.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Server {

    public void start() throws InterruptedException {
        ServerHandler serverHandler = new ServerHandler();
        EventLoopGroup eventExecutors = new NioEventLoopGroup();

        try {
            int port = 8080;
            InetSocketAddress address = new InetSocketAddress(port);
            Bootstrap server = new Bootstrap();
            server.group(eventExecutors);
            server.channel(NioDatagramChannel.class);
            server.localAddress(address);
            server.handler(new ChannelInitializer<DatagramChannel>() {
                @Override
                protected void initChannel(DatagramChannel ch) {
                    serverHandler.setEventExecutors(eventExecutors);
                    ch.pipeline().addLast(serverHandler);
                }
            });
            System.out.println("Server open on port: "+port);
            ChannelFuture channelFuture = server.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            eventExecutors.shutdownGracefully().sync();
        }
    }

    @ChannelHandler.Sharable
    public static class ServerHandler extends ChannelInboundHandlerAdapter {

        private EventLoopGroup eventExecutors;


        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            DatagramPacket packet = (DatagramPacket) msg;
            ByteBuf buffer = packet.content();

            int requestContent = buffer.readInt();
            if (requestContent == RequestContent.USER_AUTH){
                byte[] data = new byte[buffer.readableBytes()];
                buffer.readBytes(data);
                String user_id = new String(data,StandardCharsets.UTF_8);
                System.out.println(user_id);
            }
            buffer.release();
            ctx.channel().close().addListener(ChannelFutureListener.CLOSE);
            eventExecutors.shutdownGracefully();
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }

        public void setEventExecutors(EventLoopGroup eventExecutors) {
            this.eventExecutors = eventExecutors;
        }
    }
}
