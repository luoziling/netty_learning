package priv.wzb.netty.zerocopy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * NIOServer
 *
 * @author yuzuki
 * @date 2021/8/8 15:11
 * @description:
 * nio的客户端，测试零拷贝
 * @since 1.0.0
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8989));
//        serverSocketChannel.configureBlocking(false);

        ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
        while (true){
            SocketChannel socketChannel = serverSocketChannel.accept();

            long count = 0;
            while (count!=-1){
                try {
                    count += socketChannel.read(byteBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                byteBuffer.rewind();

            }
            System.out.println("count = " + count);
        }
    }
}
