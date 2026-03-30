package edu.hse.netchat.transport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class TcpTransportTest {

    @Test
    void clientCanConnectAndSendLineToServer() throws Exception {
        InetSocketAddress listen = new InetSocketAddress("127.0.0.1", 0);

        try (TcpServer server = new TcpServer(listen)) {
            InetSocketAddress bound = server.boundAddress();

            CompletableFuture<String> serverRead =
                    CompletableFuture.supplyAsync(
                            () -> {
                                try (PeerConnection conn = server.accept()) {
                                    LimitedLineReader reader =
                                            new LimitedLineReader(
                                                    new InputStreamReader(
                                                            conn.socket().getInputStream(),
                                                            StandardCharsets.UTF_8),
                                                    1024);
                                    return reader.readLine();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });

            try (PeerConnection client = new TcpClient().connect(bound, 2000)) {
                client.sendLine("hello");
            }

            String received = serverRead.get(2, TimeUnit.SECONDS);
            assertThat(received).isEqualTo("hello");
        }
    }
}
