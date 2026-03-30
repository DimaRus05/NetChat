package edu.hse.netchat.cli;

/** User input / CLI arguments error. */
public final class ArgsParserException extends RuntimeException {

    public ArgsParserException(String message) {
        super(message);
    }
}
