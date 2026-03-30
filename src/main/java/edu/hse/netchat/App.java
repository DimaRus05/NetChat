package edu.hse.netchat;

import edu.hse.netchat.cli.ArgsParser;
import edu.hse.netchat.cli.ArgsParserException;
import edu.hse.netchat.cli.CliArgs;
import edu.hse.netchat.core.ChatNode;
import edu.hse.netchat.protocol.NdjsonMessageCodec;
import java.net.InetSocketAddress;

public final class App {

    private static final int MAX_TEXT_LENGTH = 4096;
    private static final int MAX_INCOMING_LINE_CHARS = 16 * 1024;

    public static void main(String[] args) throws Exception {
        ArgsParser parser = new ArgsParser();

        final CliArgs cliArgs;
        try {
            cliArgs = parser.parse(args);
        } catch (ArgsParserException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage();
            if (message.equals(parser.helpText())) {
                System.out.println(message);
                System.exit(0);
                return;
            }

            System.err.println(message);
            System.err.println();
            System.err.println(parser.helpText());
            System.exit(2);
            return;
        }

        if (cliArgs.listen().isPresent() && cliArgs.peer().isPresent()) {
            System.err.println(
                    "Using --listen together with --peer is not supported in базовой версии.");
            System.exit(2);
            return;
        }

        ChatNode node =
                new ChatNode(
                        cliArgs.name(),
                        new NdjsonMessageCodec(MAX_TEXT_LENGTH),
                        java.time.OffsetDateTime::now,
                        MAX_INCOMING_LINE_CHARS);

        if (cliArgs.peer().isPresent()) {
            node.runConnect(cliArgs.peer().get());
        } else {
            InetSocketAddress listen =
                    cliArgs.listen().orElseGet(() -> new InetSocketAddress("0.0.0.0", 0));
            node.runListen(listen);
        }
    }
}
