package org.example;

import org.example.processing.Scaling;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StoreServerTCP {
    private static final int PORT = 8099;

    public static void main(String[] args) {

        Scaling pipeline = new Scaling(0, 1, 1, 1, 0);
        pipeline.start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TCP server started on port " + PORT);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("TCP client connected: " + socket.getInetAddress());
                    SocketWrapper wrapper = new SocketWrapper(socket, pipeline.getRawQueue());
                    pipeline.addClientConnection(wrapper);
                    new Thread(() -> {
                        try {
                            wrapper.read();
                        } finally {
                            pipeline.removeClientConnection(wrapper);
                            wrapper.close();
                        }
                    }).start();
                } catch (IOException e) {
                    System.err.println("Connection error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error occurred in TCP server", e);
        }
    }
}
