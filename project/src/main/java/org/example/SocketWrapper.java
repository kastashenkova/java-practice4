package org.example;

import org.example.annotation.NetworkChannel;
import org.example.annotation.simple.RetryOnFailure;
import org.example.annotation.simple.ThreadSafe;
import org.example.annotation.simple.VisibleForTesting;
import org.example.processing.SharedQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

@NetworkChannel(name = "METRICS")
@ThreadSafe
public class SocketWrapper {
    @VisibleForTesting
    public final Socket socket;
    private final SharedQueue<byte[]> outputQueue;
    private final DataOutputStream output;

    public SocketWrapper(Socket socket, SharedQueue<byte[]> outputQueue) throws IOException {
        this.socket = socket;
        this.outputQueue = outputQueue;
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    @VisibleForTesting
    @RetryOnFailure(attempts = 5, delayMs = 2000)
    public boolean sendPackage(byte[] p) {
        try {
            output.writeInt(p.length);
            output.write(p);
            output.flush();
            return true;
        } catch (IOException e) {
            close();
            return false;
        }
    }

    @VisibleForTesting
    @RetryOnFailure(attempts = 5, delayMs = 2000)
    public void read() {
        try (DataInputStream input = new DataInputStream(socket.getInputStream())) {
            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    int length = input.readInt();
                    byte[] data = new byte[length];
                    input.readFully(data);
                    outputQueue.produce(data);
                    sendAck();
                } catch (EOFException e) {
                    System.out.println("Connection closed by the remote host.");
                    break;
                } catch (IOException e) {
                    System.err.println("Unable to read package: " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Stream initialization failed: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Reading thread was interrupted.");
        } finally {
            close();
        }
    }

    @VisibleForTesting
    public synchronized void sendAck() throws IOException {
        output.writeUTF("ACK");
        output.flush();
    }

    @VisibleForTesting
    public void close() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close socket safely: " + e.getMessage());
        }
    }
}
