package info.kgeorgiy.ja.kadyrov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Kadyrov Rustam.
 */

public class HelloUDPNonblockingServer implements HelloServer {
    private static Selector selector;
    private static DatagramChannel datagramChannel;
    public static final int PROCESSING_TIME = 100;
    private static ExecutorService responseService;

    private static ExecutorService mainThread;


    /**
     * Creates and runs HelloNonblockingUDPServer.
     *
     * @param args First argument: port number on which requests will be received.
     *             Second argument: number of worker threads that will process requests.
     */

    public static void main(String[] args) {
//        absMain(args, HelloUDPNonblockingServer::new);
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect input, argument can't be null.");
            return;
        }
        try {
            try (HelloServer helloServer = new HelloUDPNonblockingServer()) {
                int port = Integer.parseInt(args[0]);
                int threads = Integer.parseInt(args[1]);
                helloServer.start(port, threads);
                // :NOTE: чего-нибудь подождать (например ввода с консоли)
                Thread.sleep(PROCESSING_TIME);
            }
        } catch (NumberFormatException e) {
            System.err.println("Can't parse number." + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Main thread was interrupted." + e.getMessage());
        }
    }

    /**
     * Starts a new Hello Nonblocking server.
     * This method should return immediately.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(int port, int threads) {
        try {
            selector = Selector.open();
            configureChannel(new InetSocketAddress(port));
            responseService = Executors.newFixedThreadPool(threads);
            mainThread = Executors.newSingleThreadExecutor();
            mainThread.submit(this::runTask);
        } catch (IOException e) {
            System.err.println("Error occurs while open selector.");
        }
    }

    private static void configureChannel(SocketAddress serverAddress) throws IOException {
        datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.bind(serverAddress);
        datagramChannel.register(selector, SelectionKey.OP_READ, new ServerPacket(datagramChannel.socket().getSendBufferSize()));
    }

    private void runTask() {
        while (!datagramChannel.socket().isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                selector.select();
                for (final Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    final SelectionKey key = it.next();
                    final DatagramChannel channel = (DatagramChannel) key.channel();
                    ServerPacket packet = (ServerPacket) key.attachment();
                    if (key.isReadable()) {
                        getRequest(key, channel, packet);
                    }
                    if (key.isWritable()) {
                        sendResponse(key, channel, packet);
                    }
                    it.remove();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void getRequest(SelectionKey key, DatagramChannel channel, ServerPacket packet) throws IOException {
        ByteBuffer buffer = packet.buffer;
        SocketAddress address = channel.receive(buffer.clear());
        String requestMessage = UTF_8.decode(buffer.flip()).toString();
        responseService.submit(createResponse(key, packet, address, new Response(), requestMessage));
    }

    private static Runnable createResponse(SelectionKey key, ServerPacket packet, SocketAddress address, Response response, String requestMessage) {
        return () -> {
            String responseMessage = "Hello, " + requestMessage;
            addQueue(packet, address, response, responseMessage);
            key.interestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
        };
    }

    private static void addQueue(ServerPacket packet, SocketAddress address, Response response, String responseMessage) {
        response.setMessage(responseMessage);
        response.setAddress(address);
        packet.queue.add(response);
    }

    private static void sendResponse(SelectionKey key, DatagramChannel channel, ServerPacket packet) throws IOException {
        if (!packet.queue.isEmpty()) {
            Response response = packet.queue.poll();
            channel.send(ByteBuffer.wrap(response.getMessage().getBytes(UTF_8)), response.getAddress());
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            key.interestOps(SelectionKey.OP_READ);
        }
    }


    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        responseService.close();
        try {
            datagramChannel.close();
            selector.close();
        } catch (IOException ignored) {
        }
        mainThread.close();

    }

    private static class ServerPacket {
        private final ByteBuffer buffer;
        private final Queue<Response> queue = new ConcurrentLinkedDeque<>();

        public ServerPacket(final int bufferSize) {
            this.buffer = ByteBuffer.allocate(bufferSize);
        }
    }

    private static class Response {
        private String message;
        private SocketAddress address;

        public void setMessage(String message) {
            this.message = message;
        }

        public void setAddress(SocketAddress address) {
            this.address = address;
        }

        public String getMessage() {
            return message;
        }

        public SocketAddress getAddress() {
            return address;
        }
    }
}
