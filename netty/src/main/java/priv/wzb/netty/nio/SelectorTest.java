package priv.wzb.netty.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
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
        // 置为非阻塞
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
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                // 根据事件做出对应操作
                if (selectionKey.isAcceptable()){
                    // 接收请求
                    SocketChannel socketChannel = serverSocketChannel.accept();
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
}
