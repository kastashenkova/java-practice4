package org.example.processing.receiver;

public interface Receiver extends Runnable {

    void receiveMessage();
    void stop();
}
