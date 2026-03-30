package edu.hse.netchat.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class NdjsonMessageCodecTest {

  @Test
  void encodeDecodeRoundTrip() {
    NdjsonMessageCodec codec = new NdjsonMessageCodec(4096);
    ChatMessage message = ChatMessage.chat("Alice", OffsetDateTime.parse("2026-03-30T21:15:02+03:00"), "Hi");

    String line = codec.encode(message);
    ChatMessage decoded = codec.decode(line);

    assertThat(decoded).isEqualTo(message);
  }

  @Test
  void decodeRejectsInvalidJson() {
    NdjsonMessageCodec codec = new NdjsonMessageCodec(4096);

    assertThatThrownBy(() -> codec.decode("not-json"))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("decode");
  }

  @Test
  void decodeRejectsUnsupportedType() {
    NdjsonMessageCodec codec = new NdjsonMessageCodec(4096);

    String line =
        "{\"type\":\"ping\",\"sender\":\"Alice\",\"sentAt\":\"2026-03-30T21:15:02+03:00\",\"text\":\"x\"}";

    assertThatThrownBy(() -> codec.decode(line))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("Unsupported");
  }

  @Test
  void encodeRejectsTooLongText() {
    NdjsonMessageCodec codec = new NdjsonMessageCodec(3);
    ChatMessage message = ChatMessage.chat("Alice", OffsetDateTime.now(), "1234");

    assertThatThrownBy(() -> codec.encode(message))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("too long");
  }
}
