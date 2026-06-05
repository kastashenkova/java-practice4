package org.example.encryption;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Encrypter {

    private final MessageCipher messageCipher;

    public Encrypter(MessageCipher messageCipher) {
        this.messageCipher = messageCipher;
    }

    public byte[] encrypt(Message message){
        byte[] encryptedMessage = messageCipher.encrypt(message
                .messageString()
                .getBytes(StandardCharsets.UTF_8));
        int wLen = 4 + 4 + encryptedMessage.length;
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 8 + 4 + 2 + wLen + 2);
        buffer.put((byte) 0x13);
        buffer.put(message.uniqueIdentifier());
        buffer.putLong(message.messageNumber());
        buffer.putInt(wLen);

        // 1st Crc
        byte[] header = new byte[14];
        buffer.get(0, header, 0, 14);
        buffer.putShort(Crc16.calculateCrc(header));

        // 2nd table
        buffer.putInt(message.commandId());
        buffer.putInt(message.userId());
        buffer.put(encryptedMessage);

        // 2nd Crc
        byte[] payload = new byte[wLen];
        buffer.get(16, payload, 0, wLen);
        buffer.putShort(Crc16.calculateCrc(payload));

        return buffer.array();
    }
}
