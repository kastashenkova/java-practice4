package org.example.processing.sender;

import java.net.InetAddress;

public interface Sender extends Runnable {

    void sendMessage(byte[] message, InetAddress target);
    void stop();
}
