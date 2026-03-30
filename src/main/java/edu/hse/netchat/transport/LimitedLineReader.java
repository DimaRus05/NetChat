package edu.hse.netchat.transport;

import java.io.IOException;
import java.io.Reader;

/** Reads newline-delimited lines with an explicit maximum length. */
public final class LimitedLineReader {

    private final Reader reader;
    private final int maxChars;

    public LimitedLineReader(Reader reader, int maxChars) {
        if (maxChars <= 0) {
            throw new IllegalArgumentException("maxChars must be positive");
        }
        this.reader = reader;
        this.maxChars = maxChars;
    }

    /**
     * Reads a line without the trailing '\n'.
     *
     * @return line or null on EOF
     */
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int read = reader.read();
            if (read == -1) {
                return sb.isEmpty() ? null : sb.toString();
            }
            char c = (char) read;
            if (c == '\n') {
                return sb.toString();
            }
            if (c == '\r') {
                continue;
            }
            sb.append(c);
            if (sb.length() > maxChars) {
                throw new IOException("Incoming line exceeds limit: " + maxChars);
            }
        }
    }
}
