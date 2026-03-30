package edu.hse.netchat.cli;

import java.net.InetSocketAddress;
import java.util.Optional;

public record CliArgs(
        String name, Optional<InetSocketAddress> listen, Optional<InetSocketAddress> peer) {

    public CliArgs {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("--name is required");
        }
        if (listen == null || peer == null) {
            throw new IllegalArgumentException("listen/peer must not be null");
        }
    }
}
