package info.kgeorgiy.ja.kadyrov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Rustam Kadyrov.
 */
public class HelloUDPClient implements HelloClient {

    public static final int TIMEOUT = 200;

    /**
     * Creates and runs HelloUDPClient.
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
            HelloClient client = new HelloUDPClient();
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
        try (final ExecutorService requestService = Executors.newFixedThreadPool(threads)) {
            final SocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);
            for (int i = 1; i <= threads; i++) {
                requestService.submit(getTask(address, prefix, i, requests));
            }
        } catch (UnknownHostException e) {
            System.err.println("Can't connect host:" + e.getMessage());
        }
    }

    private Runnable getTask(SocketAddress address, String prefix, int threadId, int requests) {
        return () -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TIMEOUT);
                final int bufferSize = socket.getSendBufferSize();
                final DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize, address);
                for (int request = 1; request <= requests; request++) {
                    String requestMessage = getRequestMessage(prefix, threadId, request);
                    while (!socket.isClosed() && !Thread.interrupted()) {
                        try {
                            sendPacket(socket, packet, requestMessage);
                            String responseMessage = receivePacket(socket, packet);
                            if (responseMessage.equals("Hello, " + requestMessage)) {
                                System.out.println(responseMessage);
                                break;
                            }
                        } catch (SocketTimeoutException ignored) {
                        } catch (IOException e) {
                            System.err.println("Error occurs while sending request:" + e.getMessage());
                        }
                    }
                }
            } catch (SocketException e) {
                System.err.println("Error occurs while creating socket:" + e.getMessage());
            }
        };
    }

    private static void sendPacket(DatagramSocket socket, DatagramPacket packet, String requestMessage) throws IOException {
        packet.setData(requestMessage.getBytes(StandardCharsets.UTF_8));
        socket.send(packet);
    }

    private static String receivePacket(DatagramSocket socket, DatagramPacket packet) throws IOException {
        packet.setData(new byte[socket.getSendBufferSize()]);
        socket.receive(packet);
        return getResponseMessage(packet);
    }

    private static String getResponseMessage(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    private static String getRequestMessage(String prefix, int threadId, int request) {
        return prefix + threadId + "_" + request;
    }
}
