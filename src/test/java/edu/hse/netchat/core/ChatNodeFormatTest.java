package edu.hse.netchat.core;

import static org.assertj.core.api.Assertions.assertThat;

import edu.hse.netchat.protocol.ChatMessage;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class ChatNodeFormatTest {

    @Test
    void formatIncludesTimestampSenderAndText() {
        ChatMessage message =
                ChatMessage.chat(
                        "Bob", OffsetDateTime.parse("2026-03-30T21:15:02+03:00"), "Hello!");

        String formatted = ChatNode.formatForDisplay(message);

        assertThat(formatted).isEqualTo("[2026-03-30 21:15:02] Bob: Hello!");
    }
}
