package priv.wzb.netty.goupChat;

import javax.security.auth.kerberos.KeyTab;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * GroupChatServer
 *
 * @author yuzuki
 * @date 2021/8/7 22:07
 * @description:
 * 群聊服务端
 * 实现监听客户端上线
 * 并且进行消息的读写
 * @since 1.0.0
 */
public class GroupChatServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public GroupChatServer() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(6667));
        // 开启非阻塞
        serverSocketChannel.configureBlocking(false);

        // 注册到selector 注意注册的事件类型，客户端才是CONNECT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        startListening();
    }

    private void startListening() throws IOException {
        while (true){
            // selector循环处理事件
            // select:返回一组关联到channel的selectionKey 准备好进行IO事件的处理
            int eventNumber = selector.select();
            if (eventNumber>0){
                // 获取非线程安全的selected keys（事件） keys会被删除但不会被添加
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey selectedKey = iterator.next();
                    if (selectedKey.isAcceptable()){
                        // 处理接收上线
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectedKey.channel();
                        SocketChannel sc = serverSocketChannel.accept();
                        // 必须在注册之前置为非阻塞，selector是非阻塞
                        sc.configureBlocking(false);
                        // 注册可读事件
                        sc.register(selector,SelectionKey.OP_READ);

                        // 上线提示（记录在线数）
                        System.out.println(sc.getRemoteAddress() + "上线");
                    }
                    // 发送消息
                    if (selectedKey.isReadable()){
                        sendMessage(selectedKey);
                    }

                    // 处理完就移除事件
                    iterator.remove();
                }

            }
        }
    }

    private void sendMessage(SelectionKey selectedKey) throws IOException {
        SocketChannel clientChannel = (SocketChannel) selectedKey.channel();
        // 读取消息
        try {
            // 若消息长度过长怎么处理呢 腾讯的处理方案是 文字处理过长转为文件发送
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int count = clientChannel.read(byteBuffer);
            if (count>0){
                String message = new String(byteBuffer.array());
                System.out.println("message = " + message);
                sendGroupMessage(selectedKey, message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 不能在finally中强制关闭客户端导致客户端离线
            System.out.println(clientChannel.getRemoteAddress()+"离线了。。。");
            try {
                // 发生异常 关闭通道
                selectedKey.cancel();
                clientChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void sendGroupMessage(SelectionKey selectedKey, String message) throws IOException {
        // 转发
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            if ((key.channel() instanceof SocketChannel) && key.channel() != selectedKey.channel()){
                ByteBuffer wrap = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                // 消息转发
                ((SocketChannel) key.channel()).write(wrap);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.startListening();
    }
}
