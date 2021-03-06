package com.leo.codec;

import static com.leo.bean.Command.*;
import com.leo.bean.request.*;
import com.leo.bean.Packet;
import com.leo.bean.response.*;
import com.leo.serializer.Serializer;
import com.leo.serializer.SerializerAlgorithm;
import com.leo.serializer.impl.JSONSerializer;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 数据包编解码器
 * @Author: Leo
 * @Date: 2018-11-29 下午 5:46
 */
public class PacketCodec {

    public static final int MAGIC_NUMBER = 0x12345678;   // 魔数

    private static final Map<Byte, Class<? extends Packet>> packetTypeMap;

    private static final Map<Byte, Serializer> serializerMap;

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
        packetTypeMap.put(LOGIN_RESPONSE, LoginResponsePacket.class);
        packetTypeMap.put(MESSAGE_REQUEST, MessageRequestPacket.class);
        packetTypeMap.put(MESSAGE_RESPONSE, MessageResponsePacket.class);
        packetTypeMap.put(LOGOUT_REQUEST, LogOutRequestPacket.class);
        packetTypeMap.put(LOGOUT_RESPONSE, LogOutResponsePacket.class);
        packetTypeMap.put(CREATE_GROUP_REQUEST, CreateGroupRequestPacket.class);
        packetTypeMap.put(CREATE_GROUP_RESPONSE, CreateGroupResponsePacket.class);
        packetTypeMap.put(JOIN_GROUP_REQUEST, JoinGroupRequestPacket.class);
        packetTypeMap.put(JOIN_GROUP_RESPONSE, JoinGroupResponsePacket.class);
        packetTypeMap.put(QUIT_GROUP_REQUEST, QuitGroupRequestPacket.class);
        packetTypeMap.put(QUIT_GROUP_RESPONSE, QuitGroupResponsePacket.class);
        packetTypeMap.put(LIST_GROUP_MEMBERS_REQUEST, ListGroupMembersRequestPacket.class);
        packetTypeMap.put(LIST_GROUP_MEMBERS_RESPONSE, ListGroupMembersResponsePacket.class);
        packetTypeMap.put(SEND_TO_GROUP_REQUEST, SendToGroupRequestPacket.class);
        packetTypeMap.put(SEND_TO_GROUP_RESPONSE, SendToGroupResponsePacket.class);
        packetTypeMap.put(HEART_BEAT_REQUEST, HeartBeatRequestPacket.class);
        packetTypeMap.put(HEART_BEAT_RESPONSE, HeartBeatResponsePacket.class);
        serializerMap = new HashMap<>();
        serializerMap.put(SerializerAlgorithm.JSON, new JSONSerializer());
    }

    private volatile static PacketCodec packetCodec;

    private PacketCodec() {
    }

    public static synchronized PacketCodec INSTANCE() {
        synchronized (PacketCodec.class) {
            if (packetCodec == null) {
                packetCodec = new PacketCodec();
            }
        }
        return packetCodec;
    }

    /**
     *  通信协议设计
     *
     *  -------------------------------------------------------------------------------
     *  |  魔数0x123456  |  版本号(1)  |  序列化算法  |  指令  |  数据长度  |    数据    |
     *  -------------------------------------------------------------------------------
     *      4字节            1字节         1字节       1字节      4字节         N字节
     *
     */

    /**
     * 编码
     *
     * @param byteBuf  ByteBuf
     * @param packet 数据包
     * @return byteBuf
     */
    public ByteBuf encode(ByteBuf byteBuf, Packet packet) {
        // 创建ByteBuf对象
//        ByteBuf byteBuf = alloc.ioBuffer();
        // 序列化java对象
        byte[] bytes = Serializer.DEFAULT.serializer(packet);

        // 实际编码过程
        byteBuf.writeInt(MAGIC_NUMBER); // 魔数
        byteBuf.writeByte(packet.getVersion()); // 版本
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm()); // 序列化算法
        byteBuf.writeByte(packet.getCommand()); // 指令
        byteBuf.writeInt(bytes.length); // 内容长度
        byteBuf.writeBytes(bytes);  // 内容
        return byteBuf;
    }

    /**
     * 解码
     *
     * @param byteBuf byteBuf
     * @return 数据包
     */
    public Packet decode(ByteBuf byteBuf) {
        // 跳过魔数
        byteBuf.skipBytes(4);
        // 跳过版本号
        byteBuf.skipBytes(1);
        // 序列化算法标识
        byte serializeAlgorithm = byteBuf.readByte();

        // 指令
        byte command = byteBuf.readByte();

        // 数据长度
        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        // 将数据读入bytes
        byteBuf.readBytes(bytes);

        // 获取对象类型
        Class<? extends Packet> requestType = getRequestType(command);

        // 获取序列化对象
        Serializer serializer = getSerializer(serializeAlgorithm);
        if (requestType != null && serializer != null) {
            return serializer.deserialize(requestType, bytes);
        }
        return null;
    }

    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }

    private Serializer getSerializer(byte serializeAlgorithm) {
        return serializerMap.get(serializeAlgorithm);
    }

}
