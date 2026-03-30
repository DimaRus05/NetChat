package edu.hse.netchat.protocol;

/** Codec for transferring messages over transport. */
public interface MessageCodec {

  /** Encodes a message to one-line string (without trailing newline). */
  String encode(ChatMessage message);

  /** Decodes a message from one-line string (without trailing newline). */
  ChatMessage decode(String line);
}
