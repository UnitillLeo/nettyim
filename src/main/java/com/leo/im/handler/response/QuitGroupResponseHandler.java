package com.leo.im.handler.response;

import com.leo.bean.response.QuitGroupResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Description: 退出群聊响应处理器
 * @Author: Leo
 * @Date: 2018-12-11 上午 10:49
 */
public class QuitGroupResponseHandler extends SimpleChannelInboundHandler<QuitGroupResponsePacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QuitGroupResponsePacket msg) throws Exception {
        if (msg.isSuccess()) {
            System.out.println("退出群【" + msg.getGroupId() + "】成功！");
        } else {
            System.out.println("退出群【" + msg.getGroupId() + "】失败，原因：" + msg.getReason());
        }
    }

}
