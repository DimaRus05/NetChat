package edu.hse.netchat.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;

class ArgsParserTest {

  @Test
  void helpWorksWithoutName() {
    ArgsParser parser = new ArgsParser();

    assertThatThrownBy(() -> parser.parse(new String[] {"--help"}))
        .isInstanceOf(ArgsParserException.class)
        .hasMessageContaining("Usage:");
  }

  @Test
  void requiresName() {
    ArgsParser parser = new ArgsParser();

    assertThatThrownBy(() -> parser.parse(new String[] {"--listen", "0.0.0.0:9000"}))
        .isInstanceOf(ArgsParserException.class)
        .hasMessageContaining("--name");
  }

  @Test
  void parsesListenAndPeer() {
    ArgsParser parser = new ArgsParser();

    CliArgs args =
        parser.parse(
            new String[] {
              "--name", "Alice",
              "--listen", "0.0.0.0:9000",
              "--peer", "127.0.0.1:9001"
            });

    assertThat(args.name()).isEqualTo("Alice");
    assertThat(args.listen()).contains(new InetSocketAddress("0.0.0.0", 9000));
    assertThat(args.peer()).contains(new InetSocketAddress("127.0.0.1", 9001));
  }

  @Test
  void rejectsBadHostPort() {
    ArgsParser parser = new ArgsParser();

    assertThatThrownBy(() -> parser.parse(new String[] {"--name", "A", "--peer", "noport"}))
        .isInstanceOf(ArgsParserException.class);

    assertThatThrownBy(() -> parser.parse(new String[] {"--name", "A", "--peer", "127.0.0.1:99999"}))
        .isInstanceOf(ArgsParserException.class)
        .hasMessageContaining("range");
  }
}
