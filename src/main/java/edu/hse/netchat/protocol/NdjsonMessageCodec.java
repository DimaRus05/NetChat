package edu.hse.netchat.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** NDJSON codec (one JSON object per line). */
public final class NdjsonMessageCodec implements MessageCodec {

  private final ObjectMapper objectMapper;
  private final int maxTextLength;

  public NdjsonMessageCodec(int maxTextLength) {
    if (maxTextLength <= 0) {
      throw new IllegalArgumentException("maxTextLength must be positive");
    }
    this.maxTextLength = maxTextLength;
    this.objectMapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        // Preserve original offsets/zones in ISO-8601 strings (do not coerce to mapper TZ).
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .disable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
  }

  @Override
  public String encode(ChatMessage message) {
    validate(message);
    try {
      return objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException exception) {
      throw new ProtocolException("Failed to encode message", exception);
    }
  }

  @Override
  public ChatMessage decode(String line) {
    if (line == null) {
      throw new ProtocolException("line is required");
    }
    try {
      ChatMessage message = objectMapper.readValue(line, ChatMessage.class);
      validate(message);
      return message;
    } catch (JsonProcessingException exception) {
      throw new ProtocolException("Failed to decode message", exception);
    }
  }

  private void validate(ChatMessage message) {
    if (message == null) {
      throw new ProtocolException("message is required");
    }
    if (!ChatMessage.TYPE_CHAT.equals(message.type())) {
      throw new ProtocolException("Unsupported message type: " + message.type());
    }
    if (message.text().length() > maxTextLength) {
      throw new ProtocolException("Message text is too long: " + message.text().length());
    }
  }
}
