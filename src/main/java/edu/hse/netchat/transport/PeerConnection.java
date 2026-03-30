package edu.hse.netchat.transport;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Wraps a connected socket with thread-safe sending (one line per message). */
public final class PeerConnection implements Closeable {

    private final Socket socket;
    private final BufferedWriter writer;

    public PeerConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.writer =
                new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    public Socket socket() {
        return socket;
    }

    /** Sends one line and appends '\n'. */
    public synchronized void sendLine(String line) throws IOException {
        writer.write(line);
        writer.write('\n');
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
