package priv.wzb.netty.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * SelectorTest
 *
 * @author yuzuki
 * @date 2021/8/2 21:50
 * @description:
 * @since 1.0.0
 */
public class SelectorTest {
    @Test
    public void ServerTest() throws IOException {
        // 服务端
        // 开启serverSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 开启selector
        Selector selector = Selector.open();
        // serverSocketChannel 监听端口
        serverSocketChannel.bind(new InetSocketAddress(6666));
        // 置为非阻塞 阻塞状态无法注册事件
        serverSocketChannel.configureBlocking(false);

        // 注册到selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true){
            // 开启事件监听
            if (selector.select(1000) == 0){
                System.out.println("无事发生");
                continue;
            }

            // 获取selectionKeys
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            // 流程：
            // 1.客户端连接触发acceptable事件因此进入循环找到事件为连接并处理
            // 2.结束这一轮循环，处理过程中将数据传输事件放到了selector中
            // 3.下一轮循环监听到read事件进行数据读取并展示
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                // 根据事件做出对应操作
                if (selectionKey.isAcceptable()){
                    // 接收请求
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // 设定为非阻塞
                    socketChannel.configureBlocking(false);
                    // 继续注册到selectorChannel
                    socketChannel.register(selector,SelectionKey.OP_READ,ByteBuffer.allocate(1024));
                }
                if (selectionKey.isReadable()){
                    // 从key获取channel
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    // 从key获取之前注册的buffer
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    socketChannel.read(buffer);
                    System.out.println("from 客户端:" + new String(buffer.array()));

                }

                // 事件移除selectionKey
                iterator.remove();
            }

        }

    }

    @Test
    public void client() throws IOException {
        // 连接到服务器
        SocketChannel socketChannel = SocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);
        // 非阻塞模式
        socketChannel.configureBlocking(false);
        if (!socketChannel.connect(inetSocketAddress)){
            while (!socketChannel.finishConnect()){
                System.out.println("客户端完成连接前可做别的事");
            }
            // 完成连接后通过buffer向服务端发送数据
            String str = "hello 服务器";
            ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
            socketChannel.write(byteBuffer);
        }
        System.in.read();

    }
}
