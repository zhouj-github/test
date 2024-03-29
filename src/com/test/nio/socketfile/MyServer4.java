package com.test.nio.socketfile;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * \
 * ————————————————
 * 版权声明：本文为CSDN博主「kongxx」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/kongxx/article/details/7319410
 *
 * @author zhouj
 * @since 2021-07-01
 */

public class MyServer4 {

    private final static Logger logger = Logger.getLogger(MyServer4.class.getName());

    public static void main(String[] args) {
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;

        try {
            // Selector for incoming time requests
            selector = Selector.open();

            // Create a new server socket and set to non blocking mode
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);

            // Bind the server socket to the local host and port
            serverSocketChannel.socket().setReuseAddress(true);
            serverSocketChannel.socket().bind(new InetSocketAddress(10000));

            // Register accepts on the server socket with the selector. This
            // step tells the selector that the socket wants to be put on the
            // ready list when accept operations occur, so allowing multiplexed
            // non-blocking I/O to take place.
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // Here's where everything happens. The select method will
            // return when any operations registered above have occurred, the
            // thread has been interrupted, etc.
            while (selector.select() > 0) {
                // Someone is ready for I/O, get the ready keys
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                // Walk through the ready keys collection and process date requests.
                while (it.hasNext()) {
                    SelectionKey readyKey = it.next();
                    it.remove();

                    // The key indexes into the selector so you
                    // can retrieve the socket that's ready for I/O
                    doit((ServerSocketChannel) readyKey.channel());
                }
            }
        } catch (ClosedChannelException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            try {
                selector.close();
            } catch (Exception ex) {
            }
            try {
                serverSocketChannel.close();
            } catch (Exception ex) {
            }
        }
    }

    private static void doit(final ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel socketChannel = null;
        try {
            socketChannel = serverSocketChannel.accept();

            receiveFile(socketChannel, new RandomAccessFile("/Users/zhouj/Downloads/test.mp4", "rw"));
//            sendFile(socketChannel, new File("E:/test/server_send.log"));
        } finally {
            try {
                socketChannel.close();
            } catch (Exception ex) {
            }
        }

    }

    private static void receiveFile(SocketChannel socketChannel, RandomAccessFile file) throws IOException {
        long time = System.currentTimeMillis();
        FileOutputStream fos = null;
        FileChannel channel = null;

        try {
//            fos = new FileOutputStream(file);
            channel = file.getChannel();
            MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 1024 *512);
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

            int size = 0;
            int length = 0;
            while ((size = socketChannel.read(buffer)) != -1) {
                buffer.flip();
                if (size > 0) {
                    buffer.limit(size);
//                    channel.write(buffer);
                    mappedByteBuffer.put(buffer);
                    buffer.clear();
                    length += size;
                }
            }
            System.out.println("接收文件长度:" + length);
            System.out.println("耗时:" + (System.currentTimeMillis() - time));
        } finally {
            try {
                channel.close();
            } catch (Exception ex) {
            }
            try {
                fos.close();
            } catch (Exception ex) {
            }
        }
    }

    private static void sendFile(SocketChannel socketChannel, File file) throws IOException {
        FileInputStream fis = null;
        FileChannel channel = null;
        try {
            fis = new FileInputStream(file);
            channel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            int size = 0;
            while ((size = channel.read(buffer)) != -1) {
                buffer.rewind();
                buffer.limit(size);
                socketChannel.write(buffer);
                buffer.clear();
            }
            socketChannel.socket().shutdownOutput();
        } finally {
            try {
                channel.close();
            } catch (Exception ex) {
            }
            try {
                fis.close();
            } catch (Exception ex) {
            }
        }
    }
}