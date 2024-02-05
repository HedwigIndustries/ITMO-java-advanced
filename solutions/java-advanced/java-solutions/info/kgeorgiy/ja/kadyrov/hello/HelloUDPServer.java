package info.kgeorgiy.ja.kadyrov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kadyrov Rustam.
 */

public class HelloUDPServer implements HelloServer {
    public static final int PROCESSING_TIME = 30000;
    private ExecutorService responseService;
    private DatagramSocket socket;

    /**
     * Creates and runs HelloUDPServer.
     *
     * @param args First argument: port number on which requests will be received.
     *             Second argument: number of worker threads that will process requests.
     */

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect input, argument can't be null.");
            return;
        }
        try {
            HelloServer helloServer = new HelloUDPServer();
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            helloServer.start(port, threads);
            Thread.sleep(PROCESSING_TIME);
            helloServer.close();
        } catch (NumberFormatException e) {
            System.err.println("Can't parse number." + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Main thread was interrupted." + e.getMessage());
        }
    }

    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(int port, int threads) {
        try {
            responseService = Executors.newFixedThreadPool(threads);
            socket = new DatagramSocket(port);
            final int bufferSize = socket.getReceiveBufferSize();
            final Runnable task = getTask(bufferSize);
            for (int i = 0; i < threads; i++) {
                responseService.submit(task);
            }
        } catch (SocketException e) {
            System.err.println("Error occurs while creating socket:" + e.getMessage());
        }
    }

    private Runnable getTask(final int bufferSize) {
        return () -> {
            final DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
            while (!socket.isClosed() && !Thread.interrupted()) {
                sendResponse(packet);
            }
        };
    }

    private void sendResponse(DatagramPacket packet) {
        try {
            socket.receive(packet);
            packet.setData(getPacketBytes(packet));
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error occurs while sending response message:" + e.getMessage());
        }
    }

    private static byte[] getPacketBytes(DatagramPacket packet) {
        String responseMessage = "Hello, " + getRequestMessage(packet);
        return responseMessage.getBytes(StandardCharsets.UTF_8);
    }

    private static String getRequestMessage(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        socket.close();
        responseService.close();
    }
}
