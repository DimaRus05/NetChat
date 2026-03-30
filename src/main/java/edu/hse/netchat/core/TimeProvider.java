package edu.hse.netchat.core;

import java.time.OffsetDateTime;

@FunctionalInterface
public interface TimeProvider {
  OffsetDateTime now();
}
