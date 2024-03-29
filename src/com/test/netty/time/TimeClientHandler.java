package com.test.netty.time;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author zhouj
 * @since 2020-06-17
 */
public class TimeClientHandler extends ChannelHandlerAdapter {

    private final byte[] req;
    private int counter = 0;

    public TimeClientHandler() {
        System.out.println("init TimeClientHandler");
        req = "QUERY TIME ORDER".getBytes();

    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 100; ++i) {
            ByteBuf firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
            ctx.writeAndFlush(firstMessage);
        }
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead");

        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);

        System.out.println("now is: " + new String(req, "UTF-8") + ", counter: " + counter++);

        if (counter > 99) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        ctx.close();
    }
}