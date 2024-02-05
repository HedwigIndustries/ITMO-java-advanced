package info.kgeorgiy.ja.kadyrov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @author Rustam Kadyrov.
 */
public class HelloUDPNonblockingClient implements HelloClient {

    public static final int TIMEOUT = 100;
    private static final String HELLO = "Hello, ";


    /**
     * Creates and runs HelloNonblockingUDPClient.
     *
     * @param args First argument: name or ip-address of the computer on which the server is running.
     *             Second argument: port number to send requests to.
     *             Third argument: request prefix(string).
     *             Fourth argument: the number of parallel request streams.
     *             Fifth argument: the number of requests in each thread.
     */

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Incorrect input.");
            return;
        }
        if (checkArguments(args)) {
            System.err.println("Arguments can't be null.");
            return;
        }
        String host = args[0];
        try {
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            HelloClient client = new HelloUDPNonblockingClient();
            client.run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("Can't parse number." + e.getMessage());
        }
    }

    private static boolean checkArguments(String[] args) {
        for (String arg : args) {
            if (arg == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Runs hello client.
     * This method should return when all requests are completed.
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        requests++;
        try (Selector selector = Selector.open()) {
            final SocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);
            for (int i = 1; i <= threads; i++) {
                createChannel(selector, address, i);
            }
            runSelector(selector, address, prefix, requests);
        } catch (IOException e) {
            System.err.println("Errors occurs with opening selector.");
        }
    }

    private static void createChannel(Selector selector, SocketAddress address, int i) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.connect(address);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE, new ChannelPacket(i, channel.socket().getSendBufferSize()));
    }

    private static void runSelector(Selector selector, SocketAddress address, String prefix, int requests) throws IOException {
        while (!selector.keys().isEmpty() && !Thread.interrupted()) {
            selector.select(TIMEOUT);
            if (selector.selectedKeys().isEmpty()) {
                repeatRequest(selector);
                continue;
            }
            for (final Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                final SelectionKey key = it.next();
                final DatagramChannel channel = (DatagramChannel) key.channel();
                ChannelPacket packet = (ChannelPacket) key.attachment();
                String requestMessage = getRequestMessage(prefix, packet.id, packet.requests);
                if (key.isWritable()) {
                    sendPacket(address, key, channel, requestMessage);
                }
                if (key.isReadable()) {
                    receivePacket(requests, key, channel, packet, requestMessage);
                }
                it.remove();
            }
        }
    }


    private static void repeatRequest(Selector selector) {
        for (SelectionKey key : selector.keys()) {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private static String getRequestMessage(String prefix, int threadId, int request) {
        return prefix + threadId + "_" + request;
    }

    private static void sendPacket(SocketAddress address, SelectionKey key, DatagramChannel channel, String requestMessage) throws IOException {
        channel.send(ByteBuffer.wrap(requestMessage.getBytes(StandardCharsets.UTF_8)), address);
        key.interestOps(SelectionKey.OP_READ);
    }

    private static void receivePacket(int requests, SelectionKey key, DatagramChannel channel, ChannelPacket packet, String requestMessage) throws IOException {
        channel.receive(packet.buffer.clear());
        String responseMessage = StandardCharsets.UTF_8.decode(packet.buffer.flip()).toString();
        checkResponse(packet, requestMessage, responseMessage);
        changeKeyInterest(requests, key, packet);
    }

    private static void checkResponse(ChannelPacket packet, String requestMessage, String responseMessage) {
        if (responseMessage.equals(HELLO + requestMessage)) {
            System.out.println(responseMessage);
            packet.requests++;
        }
    }

    private static void changeKeyInterest(int requests, SelectionKey key, ChannelPacket packet) {
        if (packet.requests == requests) {
            key.cancel();
        } else {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private static class ChannelPacket {
        private final int id;
        private int requests = 1;
        private final ByteBuffer buffer;

        public ChannelPacket(final int id, final int bufferSize) {
            this.id = id;
            this.buffer = ByteBuffer.allocate(bufferSize);
        }
    }
}
