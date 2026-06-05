package org.example.encryption;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Decrypter {

    private final MessageCipher messageCipher;

    public Decrypter(MessageCipher messageCipher) {
        this.messageCipher = messageCipher;
    }

    public Message decrypt(byte[] messageToDecrypt){
        ByteBuffer buffer = ByteBuffer.wrap(messageToDecrypt);
        byte magicByte = buffer.get();
        if (magicByte != 0x13) {
            throw new IllegalArgumentException(
                    "Invalid magic byte: expected 0x13, got 0x"
                            + Integer.toHexString(magicByte & 0xFF));
        }

        byte uniqueIdentifierByte = buffer.get();
        long messageNumber = buffer.getLong();
        int wlen = buffer.getInt();
        short firstCrc = buffer.getShort();

        short checkSum = Crc16.calculateCrc(messageToDecrypt, 0, 14);
        validateChecksum(checkSum, firstCrc);

        int commandId = buffer.getInt();
        int userId = buffer.getInt();

        byte[] payload = new byte[wlen];
        buffer.get(16, payload, 0, wlen);

        short secondCrc = buffer.getShort(16 + wlen);
        short checkSum2 = Crc16.calculateCrc(payload);
        validateChecksum(checkSum2, secondCrc);

        String decryptedMessage = new String(
                messageCipher.decrypt(
                        Arrays.copyOfRange(payload, 8, wlen)), StandardCharsets.UTF_8);

        return new Message(uniqueIdentifierByte, messageNumber, commandId, userId, decryptedMessage);
    }

    private void validateChecksum(short checkSum, short secondCrc){
        if (checkSum != secondCrc){
            throw new IllegalArgumentException("Checksum does not match");
        }
    }
}
