package edu.hse.netchat.protocol;

/** Protocol/codec level error (invalid input, unsupported message, etc.). */
public final class ProtocolException extends RuntimeException {

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
