package org.example.encryption;

public record Message(byte uniqueIdentifier,
                      long messageNumber,
                      int commandId,
                      int userId,
                      String messageString) {
}
