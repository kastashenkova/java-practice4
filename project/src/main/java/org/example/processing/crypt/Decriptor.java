package org.example.processing.crypt;

import org.example.encryption.Decrypter;
import org.example.encryption.Message;
import org.example.encryption.MessageCipher;
import org.example.processing.SharedQueue;

public class Decriptor implements Runnable {
    private final Decrypter decrypter = new Decrypter(new MessageCipher());
    private final SharedQueue<byte[]> inputQueue;
    private final SharedQueue<Message>  outputQueue;
    private volatile boolean active = true;

    public Decriptor(SharedQueue<byte[]> inputQueue, SharedQueue<Message> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    public void decript(byte[] message) {
        Message decoded = decrypter.decrypt(message);
        try {
            outputQueue.produce(decoded);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (active && !Thread.currentThread().isInterrupted()) {
            try {
                byte[] message = inputQueue.consume();
                decript(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        active = false;
    }
}
