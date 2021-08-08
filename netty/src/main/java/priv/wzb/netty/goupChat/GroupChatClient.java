package priv.wzb.netty.goupChat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

import static java.lang.Thread.sleep;

/**
 * GroupChatClient
 *
 * @author yuzuki
 * @date 2021/8/7 23:00
 * @description:
 * @since 1.0.0
 */
public class GroupChatClient {
    private Selector selector;
    private SocketChannel client;
    private String username;

    public GroupChatClient() throws IOException {
        selector = Selector.open();
        client = SocketChannel.open(new InetSocketAddress("127.0.0.1",6667));
        // 设置非阻塞
        client.configureBlocking(false);

        // 注册读写
        client.register(selector, SelectionKey.OP_READ);
        username = client.getLocalAddress().toString();
        System.out.println(username+"客户端连接");
    }

    public void sendInfo(String message) throws IOException {
        // 包装message
        message = username + "说：" +message;
        ByteBuffer wrap = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
        client.write(wrap);
    }

    public void readInfo(){
        try {
            // 直接select是阻塞
            int select = selector.select();
            if (select>0){
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    if (key.isReadable()){
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int read = channel.read(byteBuffer);
                        if (read>0){
                            String acceptMessage = new String(byteBuffer.array());
                            System.out.println("acceptMessage = " + acceptMessage);
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        GroupChatClient chatClient = new GroupChatClient();
        // 开启子线程不断读取，主线程阻塞式向服务端发送消息
        new Thread(()->{
            while (true) {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                chatClient.readInfo();
                System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String s = scanner.nextLine();
            chatClient.sendInfo(s);
        }
    }
}
