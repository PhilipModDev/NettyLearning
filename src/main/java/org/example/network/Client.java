package org.example.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Client {

    protected final InetSocketAddress address = new InetSocketAddress("192.168.1.10", 8080);

    public void start(){
        ClientHandler clientHandler = new ClientHandler(this);
        EventLoopGroup loopGroup = new NioEventLoopGroup(0);
        try {
            Bootstrap client = new Bootstrap();
            client.group(loopGroup);
            client.channel(NioDatagramChannel.class);
            client.remoteAddress(address);
            client.handler(new ChannelInitializer<DatagramChannel>() {
                @Override
                protected void initChannel(DatagramChannel socketChannel) {
                    socketChannel.pipeline().addLast(clientHandler);
                }
            });
            System.out.println("Connecting to server...");
            ChannelFuture future = client.connect().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            loopGroup.shutdownGracefully();
        }
    }

    @ChannelHandler.Sharable
    public static class ClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {


        private final Client client;

        public ClientHandler(Client client){
            this.client = client;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("Connected.");
            PooledByteBufAllocator pooledByteBufAllocator = (PooledByteBufAllocator) ctx.alloc();
            ByteBuf buffer = pooledByteBufAllocator.buffer();

            buffer.writeInt(RequestContent.USER_AUTH);
            String user_id = UUID.randomUUID().toString();
            byte[] data = user_id.getBytes(StandardCharsets.UTF_8);
            buffer.writeBytes(data);
            DatagramPacket packet = new DatagramPacket(buffer,client.address);
            ctx.writeAndFlush(packet);
            System.out.println("Request sent on: "+ ctx.channel().remoteAddress());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket byteBuf) {

        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    }
}
