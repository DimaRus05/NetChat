package edu.hse.netchat.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class NdjsonMessageCodecTest {

    @Test
    void encodeDecodeRoundTrip() {
        NdjsonMessageCodec codec = new NdjsonMessageCodec(4096);
        ChatMessage message =
                ChatMessage.chat("Alice", OffsetDateTime.parse("2026-03-30T21:15:02+03:00"), "Hi");

        String line = codec.encode(message);
        ChatMessage decoded = codec.decode(line);

        assertThat(decoded.type()).isEqualTo(message.type());
        assertThat(decoded.sender()).isEqualTo(message.sender());
        assertThat(decoded.text()).isEqualTo(message.text());
        assertThat(decoded.sentAt().toInstant()).isEqualTo(message.sentAt().toInstant());
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

    @Test
    void decodeRejectsMissingSender() {
        NdjsonMessageCodec codec = new NdjsonMessageCodec(4096);

        String line = "{\"type\":\"chat\",\"sentAt\":\"2026-03-30T21:15:02+03:00\",\"text\":\"x\"}";

        assertThatThrownBy(() -> codec.decode(line)).isInstanceOf(ProtocolException.class);
    }

    @Test
    void decodeRejectsBlankSender() {
        NdjsonMessageCodec codec = new NdjsonMessageCodec(4096);

        String line =
                "{\"type\":\"chat\",\"sender\":\"\",\"sentAt\":\"2026-03-30T21:15:02+03:00\",\"text\":\"x\"}";

        assertThatThrownBy(() -> codec.decode(line)).isInstanceOf(ProtocolException.class);
    }

    @Test
    void decodeRejectsNullText() {
        NdjsonMessageCodec codec = new NdjsonMessageCodec(4096);

        String line =
                "{\"type\":\"chat\",\"sender\":\"Alice\",\"sentAt\":\"2026-03-30T21:15:02+03:00\",\"text\":null}";

        assertThatThrownBy(() -> codec.decode(line)).isInstanceOf(ProtocolException.class);
    }

    @Test
    void decodeRejectsNullSentAt() {
        NdjsonMessageCodec codec = new NdjsonMessageCodec(4096);

        String line = "{\"type\":\"chat\",\"sender\":\"Alice\",\"sentAt\":null,\"text\":\"x\"}";

        assertThatThrownBy(() -> codec.decode(line)).isInstanceOf(ProtocolException.class);
    }
}
