package priv.wzb.netty.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * NIOClient
 *
 * @author yuzuki
 * @date 2021/8/8 15:17
 * @description:
 * @since 1.0.0
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8989));
        socketChannel.configureBlocking(false);

        // 从文件中读取进行零拷贝
        FileInputStream fileInputStream = new FileInputStream("pic-test.jfif");
        FileChannel fileChannel = fileInputStream.getChannel();
        long size = fileChannel.size();
        ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
        int count = (int) ((size+8192-1)/8192);
        int i = 0;
        long totalTransferSize = 0;
        long start = System.currentTimeMillis();
        while (i<count){
            // 分批传送
            // windows下无法一次性transferTo
            totalTransferSize+=fileChannel.transferTo(i*8192,8192,socketChannel);
            i++;
        }
        System.out.println("times:" + (System.currentTimeMillis()-start));
        System.out.println("totalTransferSize = " + totalTransferSize);
    }
}
