package edu.hse.netchat.cli;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Minimal args parser for: --name, --listen host:port, --peer host:port. */
public final class ArgsParser {

    public CliArgs parse(String[] args) {
        Map<String, String> options = toOptions(args);

        if (options.containsKey("--help")) {
            throw new ArgsParserException(helpText());
        }

        String name = options.get("--name");
        if (name == null || name.isBlank()) {
            throw new ArgsParserException("Missing required argument: --name");
        }

        Optional<InetSocketAddress> listen =
                Optional.ofNullable(options.get("--listen")).map(this::parseHostPort);
        Optional<InetSocketAddress> peer =
                Optional.ofNullable(options.get("--peer")).map(this::parseHostPort);

        return new CliArgs(name.trim(), listen, peer);
    }

    private Map<String, String> toOptions(String[] args) {
        Map<String, String> options = new HashMap<>();
        if (args == null) {
            return options;
        }
        for (int index = 0; index < args.length; index++) {
            String key = args[index];
            if (!key.startsWith("--")) {
                throw new ArgsParserException("Unexpected token: " + key);
            }
            if (key.equals("--help")) {
                options.put("--help", "true");
                continue;
            }
            if (index + 1 >= args.length) {
                throw new ArgsParserException("Missing value for: " + key);
            }
            String value = args[++index];
            options.put(key, value);
        }
        return options;
    }

    private InetSocketAddress parseHostPort(String value) {
        if (value == null || value.isBlank()) {
            throw new ArgsParserException("Empty address");
        }
        String trimmed = value.trim();

        int colon = trimmed.lastIndexOf(':');
        if (colon < 0) {
            throw new ArgsParserException("Expected host:port, got: " + trimmed);
        }

        String host = trimmed.substring(0, colon);
        String portStr = trimmed.substring(colon + 1);

        if (host.isBlank()) {
            throw new ArgsParserException("Host is empty in: " + trimmed);
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException exception) {
            throw new ArgsParserException("Port is not a number in: " + trimmed);
        }

        if (port < 0 || port > 65535) {
            throw new ArgsParserException("Port out of range in: " + trimmed);
        }

        return new InetSocketAddress(host, port);
    }

    public String helpText() {
        return "Usage: java -jar netchat.jar --name <username> [--listen host:port] [--peer"
                + " host:port]\n"
                + "\n"
                + "Modes:\n"
                + "  - If --peer is set: connect to peer and start chat\n"
                + "  - If --peer is not set: listen (default listen is 0.0.0.0:0 if --listen"
                + " omitted)\n";
    }
}
