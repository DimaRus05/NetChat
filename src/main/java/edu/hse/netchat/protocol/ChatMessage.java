package edu.hse.netchat.protocol;

import java.time.OffsetDateTime;

/**
 * Chat message transferred between peers.
 *
 * <p>Wire format: NDJSON (one JSON object per line, UTF-8), see {@link NdjsonMessageCodec}.
 */
public record ChatMessage(String type, String sender, OffsetDateTime sentAt, String text) {

  public static final String TYPE_CHAT = "chat";

  public ChatMessage {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("type is required");
    }
    if (sender == null || sender.isBlank()) {
      throw new IllegalArgumentException("sender is required");
    }
    if (sentAt == null) {
      throw new IllegalArgumentException("sentAt is required");
    }
    if (text == null) {
      throw new IllegalArgumentException("text is required");
    }
  }

  public static ChatMessage chat(String sender, OffsetDateTime sentAt, String text) {
    return new ChatMessage(TYPE_CHAT, sender, sentAt, text);
  }
}
