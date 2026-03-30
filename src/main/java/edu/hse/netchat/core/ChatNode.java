package edu.hse.netchat.core;

import edu.hse.netchat.protocol.ChatMessage;
import edu.hse.netchat.protocol.MessageCodec;
import edu.hse.netchat.protocol.ProtocolException;
import edu.hse.netchat.transport.LimitedLineReader;
import edu.hse.netchat.transport.PeerConnection;
import edu.hse.netchat.transport.TcpClient;
import edu.hse.netchat.transport.TcpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Single-connection chat node.
 *
 * <p>Base scope: exactly one active peer connection.
 */
public final class ChatNode {

    private static final Logger logger = Logger.getLogger(ChatNode.class.getName());
    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String userName;
    private final MessageCodec codec;
    private final TimeProvider timeProvider;
    private final int maxIncomingLineChars;

    public ChatNode(
            String userName,
            MessageCodec codec,
            TimeProvider timeProvider,
            int maxIncomingLineChars) {
        this.userName = Objects.requireNonNull(userName);
        this.codec = Objects.requireNonNull(codec);
        this.timeProvider = Objects.requireNonNull(timeProvider);
        if (maxIncomingLineChars <= 0) {
            throw new IllegalArgumentException("maxIncomingLineChars must be positive");
        }
        this.maxIncomingLineChars = maxIncomingLineChars;
    }

    public void runListen(InetSocketAddress listenAddress) throws IOException {
        try (TcpServer server = new TcpServer(listenAddress)) {
            InetSocketAddress bound = server.boundAddress();
            System.out.println("Listening on " + bound.getHostString() + ":" + bound.getPort());

            try (PeerConnection connection = server.accept()) {
                System.out.println(
                        "Peer connected: " + connection.socket().getRemoteSocketAddress());
                runChat(connection);
            }
        }
    }

    public void runConnect(InetSocketAddress peerAddress) throws IOException {
        try (PeerConnection connection = new TcpClient().connect(peerAddress, 5000)) {
            System.out.println(
                    "Connected to peer: " + connection.socket().getRemoteSocketAddress());
            runChat(connection);
        }
    }

    private void runChat(PeerConnection connection) throws IOException {
        CountDownLatch done = new CountDownLatch(1);

        Thread readerThread = new Thread(() -> readLoop(connection, done), "netchat-socket-reader");
        readerThread.setDaemon(true);
        readerThread.start();

        try (BufferedReader stdin =
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while (true) {
                String text = stdin.readLine();
                if (text == null) {
                    break;
                }
                String trimmed = text.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.equalsIgnoreCase("/exit")) {
                    break;
                }

                OffsetDateTime now = timeProvider.now();
                ChatMessage message = ChatMessage.chat(userName, now, trimmed);

                String line;
                try {
                    line = codec.encode(message);
                } catch (ProtocolException ex) {
                    System.err.println("Cannot send message: " + ex.getMessage());
                    continue;
                }

                try {
                    connection.sendLine(line);
                    printMessage(message);
                } catch (IOException ex) {
                    System.err.println("Send failed: " + ex.getMessage());
                    logger.log(Level.WARNING, "Send failed", ex);
                    break;
                }
            }
        } finally {
            done.countDown();
            try {
                connection.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    private void readLoop(PeerConnection connection, CountDownLatch done) {
        try {
            LimitedLineReader reader =
                    new LimitedLineReader(
                            new InputStreamReader(
                                    connection.socket().getInputStream(), StandardCharsets.UTF_8),
                            maxIncomingLineChars);

            while (done.getCount() > 0) {
                String line = reader.readLine();
                if (line == null) {
                    System.out.println("Peer disconnected.");
                    return;
                }

                try {
                    ChatMessage message = codec.decode(line);
                    printMessage(message);
                } catch (ProtocolException ex) {
                    System.err.println("Invalid incoming message: " + ex.getMessage());
                    logger.log(Level.WARNING, "Invalid incoming message", ex);
                }
            }
        } catch (IOException ex) {
            if (done.getCount() > 0) {
                System.err.println("Connection error: " + ex.getMessage());
                logger.log(Level.WARNING, "Connection error", ex);
            }
        }
    }

    private void printMessage(ChatMessage message) {
        String time = DISPLAY_TIME.format(message.sentAt().toLocalDateTime());
        System.out.println("[" + time + "] " + message.sender() + ": " + message.text());
    }
}
