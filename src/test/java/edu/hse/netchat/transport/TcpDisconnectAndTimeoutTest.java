package edu.hse.netchat.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class TcpDisconnectAndTimeoutTest {

    @Test
    void serverSeesEofWhenClientDisconnects() throws Exception {
        InetSocketAddress listen = new InetSocketAddress("127.0.0.1", 0);

        try (TcpServer server = new TcpServer(listen)) {
            InetSocketAddress bound = server.boundAddress();

            try (PeerConnection client = new TcpClient().connect(bound, 2000);
                    PeerConnection serverSide = server.accept()) {
                // Close client end first -> server side should see EOF.
                client.close();

                LimitedLineReader reader =
                        new LimitedLineReader(
                                new InputStreamReader(
                                        serverSide.socket().getInputStream(),
                                        StandardCharsets.UTF_8),
                                1024);

                assertThat(reader.readLine()).isNull();
            }
        }
    }

    @Test
    void connectRespectsTimeoutOrFailsFast() {
        InetSocketAddress unroutable = new InetSocketAddress("192.0.2.1", 65000);

        long startNanos = System.nanoTime();
        assertThatThrownBy(() -> new TcpClient().connect(unroutable, 100))
                .isInstanceOf(Exception.class);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startNanos);

        // Be lenient across OS/network stacks but still ensure we don't hang.
        assertThat(elapsed).isLessThan(Duration.ofSeconds(2));
    }
}
